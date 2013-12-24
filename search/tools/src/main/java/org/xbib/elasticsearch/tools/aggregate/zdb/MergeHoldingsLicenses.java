/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.elasticsearch.tools.aggregate.zdb;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import org.xbib.util.DateUtil;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.IngestClient;
import org.xbib.elasticsearch.support.client.SearchClient;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.Manifestation;
import org.xbib.pipeline.ElementQueuePipelineExecutor;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.util.Strings;
import org.xbib.util.URIUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.elasticsearch.tools.aggregate.WrappedSearchHit;
import org.xbib.options.OptionParser;
import org.xbib.options.OptionSet;
import org.xbib.util.ExceptionFormatter;

import static com.google.common.collect.Sets.newSetFromMap;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Merge ZDB title and holdings and EZB licenses
 *
 */
public class MergeHoldingsLicenses extends ElementQueuePipelineExecutor<Boolean, Manifestation, MergeHoldingsLicensesPipeline, WrappedSearchHit> {

    private final static Logger logger = LoggerFactory.getLogger(MergeHoldingsLicenses.class.getSimpleName());

    private final static String lf = System.getProperty("line.separator");

    private final MergeHoldingsLicenses service;

    private Client client;

    private Ingest ingest;

    private URI sourceURI;

    private URI targetURI;

    private String sourceTitleIndex;

    private String sourceTitleType;

    private int size;

    private long millis;

    private String identifier;

    private Set<String> docs;

    private Set<String> clusters;

    private Set<String> manifestations;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("source").withRequiredArg().ofType(String.class).required();
                    accepts("target").withRequiredArg().ofType(String.class).required();
                    accepts("shards").withOptionalArg().ofType(Integer.class).defaultsTo(3);
                    accepts("replica").withOptionalArg().ofType(Integer.class).defaultsTo(0);
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors() * 4);
                    accepts("concurrency").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors() * 4);
                    accepts("scansize").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("scanmillis").withRequiredArg().ofType(Long.class).defaultsTo(3600L * 1000L);
                    accepts("id").withOptionalArg().ofType(String.class);
                }
            };
            OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + MergeHoldingsLicenses.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --source <uri>         URI for connecting to the Elasticsearch source" + lf
                        + " --target <uri>         URI for connecting to Elasticsearch target" + lf
                        + " --shards <n>           number of shards" + lf
                        + " --replica <n>          number of replica" + lf
                        + " --mock <bool>          dry run of indexing (optional, default: false)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 1000)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: number of cpu core * 4)"
                        + " --concurrency <n>      merge thread concurrency (optional, default: number of cpu cores * 4)"
                        + " --scansize <n>         size for scan query result (optional, default: 1000)"
                        + " --scanmillis <ms>      life time in milliseconds for scan query (optional, default: 60000)"
                        + " --id <n>               ZDB ID (optional, default is all ZDB IDs)"
                );
                System.exit(1);
            }

            URI sourceURI = URI.create((String)options.valueOf("source"));
            URI targetURI = URI.create((String)options.valueOf("target"));
            Integer maxBulkActions = (Integer) options.valueOf("maxbulkactions");
            Integer maxConcurrentBulkRequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            Integer shards = (Integer)options.valueOf("shards");
            Integer replica = (Integer)options.valueOf("replica");
            Integer concurrency = (Integer) options.valueOf("concurrency");
            if (concurrency < 1) {
                concurrency = 1;
            }
            if (concurrency > 256) {
                concurrency = 256;
            }
            Integer size = (Integer) options.valueOf("scansize");
            Long millis = (Long) options.valueOf("scanmillis");
            String identifier = (String) options.valueOf("id");

            logger.info("connecting to search source {}...", sourceURI);

            SearchClient search = new SearchClient()
                    .newClient(sourceURI);

            logger.info("connecting to target index {} ...", targetURI);

            Ingest ingest = new IngestClient()
                    .maxActionsPerBulkRequest(maxBulkActions)
                    .maxConcurrentBulkRequests(maxConcurrentBulkRequests)
                    .setIndex(URIUtil.parseQueryString(targetURI).get("index"))
                    .setting(MergeHoldingsLicenses.class.getResourceAsStream("transport-client-settings.json"))
                    .newClient(targetURI)
                    .waitForCluster()
                    //.setting(MergeWithLicenses.class.getResourceAsStream("index-settings.json"))
                            //.mapping("works", MergeWithLicenses.class.getResourceAsStream("works.json"))
                            //.mapping("manifestations", MergeWithLicenses.class.getResourceAsStream("manifestations.json"))
                            //.mapping("volumes", MergeWithLicenses.class.getResourceAsStream("volumes.json"))
                    .newIndex()
                    .refresh()
                    .shards(shards)
                    .replica(replica)
                    .startBulk();

            MergeHoldingsLicenses merge = new MergeHoldingsLicenses(search, ingest)
                    .sourceURI(sourceURI)
                    .targetURI(targetURI)
                    .provider()
                    .concurrency(concurrency)
                    .size(size)
                    .millis(millis)
                    .identifier(identifier)
                    .prepare()
                    .execute();

            logger.info("merge shutdown in progress");
            merge.shutdown(new WrappedSearchHit().set(null));

            for (Pipeline p : merge.getPipelines()) {
                logger.info("pipeline {}, started {}, ended {}, took {}",
                        p,
                        DateUtil.formatDateISO(new Date(p.startedAt())),
                        DateUtil.formatDateISO(new Date(p.stoppedAt())),
                        TimeValue.timeValueMillis(p.took()).format());
            }

            /*long d = writeCounter.get(); //number of documents written
            long bytes = ingest.getVolumeInBytes();
            double dps = d * 1000.0 / (double)(t1 - t0);
            double avg = bytes / (d + 1.0); // avoid div by zero
            double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
            String t = TimeValue.timeValueMillis(t1 - t0).format();
            String byteSize = FormatUtil.convertFileSize(bytes);
            String avgSize = FormatUtil.convertFileSize(avg);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            logger.info("Merging complete. {} docs written, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
                    d,
                    t,
                    (t1-t0),
                    byteSize,
                    bytes,
                    avgSize,
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));

            double qps = queryCounter.get() * 1000.0 / (double)(t1 - t0);
            logger.info("queries={} qps={} hits={} manifestations={} holdings={} licenses={}",
                    queryCounter.get(),
                    formatter.format(qps),
                    hitCounter.get(),
                    manifestationCounter.get(),
                    holdingCounter.get(),
                    indicatorCounter.get());*/


            logger.info("ingest shutdown in progress");
            ingest.shutdown();

            logger.info("search shutdown in progress");
            search.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("exit with error");
            System.exit(1);
        }
        logger.info("exit with success");
        System.exit(0);
    }

    private MergeHoldingsLicenses(SearchClient search, Ingest ingest) {
        this.docs = newSetFromMap(new ConcurrentHashMap<String,Boolean>());
        this.clusters = newSetFromMap(new ConcurrentHashMap<String,Boolean>());
        this.manifestations = newSetFromMap(new ConcurrentHashMap<String,Boolean>());
        this.service = this;
        this.client = search.client();
        this.ingest = ingest;
    }

    public Client client() {
        return client;
    }

    public Ingest ingest() {
        return ingest;
    }

    public MergeHoldingsLicenses sourceURI(URI sourceURI) {
        this.sourceURI = sourceURI;
        Map<String,String> params = URIUtil.parseQueryString(sourceURI);
        this.sourceTitleIndex = params.get("bibIndex");
        this.sourceTitleType = params.get("bibType");
        if (Strings.isNullOrEmpty(sourceTitleIndex)) {
            throw new IllegalArgumentException("no bibIndex parameter given");
        }
        if (Strings.isNullOrEmpty(sourceTitleType)) {
            throw new IllegalArgumentException("no bibType parameter given");
        }
        return this;
    }

    public URI sourceURI() {
        return sourceURI;
    }

    public URI targetURI() {
        return targetURI;
    }

    public MergeHoldingsLicenses targetURI(URI targetURI) {
        this.targetURI = targetURI;
        return this;
    }

    public MergeHoldingsLicenses size(int size) {
        this.size = size;
        return this;
    }

    public int size() {
        return size;
    }

    public MergeHoldingsLicenses millis(long millis) {
        this.millis = millis;
        return this;
    }

    public long millis() {
        return millis;
    }

    @Override
    public MergeHoldingsLicenses concurrency(int concurrency) {
        super.concurrency(concurrency);
        return this;
    }

    public MergeHoldingsLicenses identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public MergeHoldingsLicenses provider() {
        super.provider(new PipelineProvider<MergeHoldingsLicensesPipeline>() {
            int i = 0;

            @Override
            public MergeHoldingsLicensesPipeline get() {
                return new MergeHoldingsLicensesPipeline(service, i++);
            }
        });
        return this;
    }

    @Override
    public MergeHoldingsLicenses prepare() {
        super.prepare();
        return this;
    }

    @Override
    public MergeHoldingsLicenses execute() {
        // execute merge pipelines
        super.execute();
        if (logger.isDebugEnabled()) {
            logger.debug("aggregate executing");
        }
        // enter loop over all manifestations
        boolean failure = false;
        boolean complete = false;
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        // single identifier?
        if (identifier != null) {
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("aggregate request = {}", searchRequest.toString());
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        long total = searchResponse.getHits().getTotalHits();
        long count = 0L;
        double percentage;
        if (logger.isDebugEnabled()) {
            logger.debug("hits={}", searchResponse.getHits().getTotalHits());
        }
        while (!failure && !complete && searchResponse.getScrollId() != null) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                try {
                    if (canReceive() == 0L) {
                        logger.error("no more pipelines left to receive, aborting");
                        complete = true;
                        break;
                    }
                    queue().offer(new WrappedSearchHit().set(hit), 60, TimeUnit.SECONDS);
                    count++;
                    percentage = count * 100 / total;
                    logger.debug("{}/{} {} percentage completed, docs = {}",
                            count, total, percentage,  docs.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("interrupted, queue no longer active");
                    failure = true;
                    break;
                } catch (Throwable e) {
                    logger.error("error passing data to merge pipelines, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    failure = true;
                    break;
                }
            }
        }
        return this;
    }

    public Set<String> docs() {
        return docs;
    }

    public Set<String> clusters() {
        return clusters;
    }

    public Set<String> manifestations() {
        return manifestations;
    }
}
