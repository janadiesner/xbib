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
package org.xbib.tools.merge.zdb.citations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.transport.BulkTransportClient;
import org.xbib.elasticsearch.support.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.support.client.mock.MockTransportClient;
import org.xbib.elasticsearch.support.client.search.SearchClient;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.queue.QueuePipelineExecutor;
import org.xbib.tools.CommandLineInterpreter;
import org.xbib.tools.merge.zdb.entities.Manifestation;
import org.xbib.tools.util.SearchHitPipelineElement;
import org.xbib.util.DateUtil;
import org.xbib.util.ExceptionFormatter;

import java.io.Reader;
import java.io.Writer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Sets.newSetFromMap;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;

/**
 * Merge ZDB with citation database
 */
public class WithCitations
        extends QueuePipelineExecutor<Boolean, Manifestation, WithCitationsPipeline, SearchHitPipelineElement>
        implements CommandLineInterpreter {

    private final static Logger logger = LogManager.getLogger(WithCitations.class.getName());

    private static Settings settings;

    private Set<String> docs;

    private Client client;

    private Ingest ingest;

    private WithCitations service;

    private String serialIndex;
    private String serialType;

    private int size;
    private long millis;
    private String identifier;

    public WithCitations reader(Reader reader) {
        settings = settingsBuilder().loadFromReader(reader).build();
        return this;
    }

    public WithCitations settings(Settings newSettings) {
        settings = newSettings;
        return this;
    }

    public WithCitations writer(Writer writer) {
        return this;
    }

    public WithCitations prepare() {
        super.prepare();
        docs = newSetFromMap(new ConcurrentHashMap<String, Boolean>(16, 0.75f, settings.getAsInt("concurrency", 1)));
        return this;
    }

    @Override
    public void run() throws Exception {
        this.serialIndex = settings.get("serialIndex");
        this.serialType = settings.get("serialType");
        SearchClient search = new SearchClient().newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("source.cluster"))
                .put("host", settings.get("source.host"))
                .put("port", settings.getAsInt("source.port", 9300))
                .put("sniff", settings.getAsBoolean("source.sniff", false))
                .build());
        try {
            this.service = this;
            this.client = search.client();
            this.size = settings.getAsInt("scrollSize", 10);
            this.millis = settings.getAsTime("scrollTimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
            this.identifier = settings.get("identifier");

            this.ingest = settings.getAsBoolean("mock", false) ?
                    new MockTransportClient() :
                    "ingest".equals(settings.get("client")) ?
                            new IngestTransportClient() :
                            new BulkTransportClient();
            ingest.maxActionsPerBulkRequest(settings.getAsInt("maxBulkActions", 100))
                    .maxConcurrentBulkRequests(settings.getAsInt("maxConcurrentBulkRequests",
                            Runtime.getRuntime().availableProcessors()));

            ingest.setting(WithCitations.class.getResourceAsStream("transport-client-settings.json"));
            ingest.newClient(ImmutableSettings.settingsBuilder()
                    .put("cluster.name", settings.get("target.cluster"))
                    .put("host", settings.get("target.host"))
                    .put("port", settings.getAsInt("target.port", 9300))
                    .put("sniff", settings.getAsBoolean("target.sniff", false))
                    .build());
            ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
            // TODO create settings/mappings
            //.setting(MergeWithLicenses.class.getResourceAsStream("index-settings.json"))
            //.mapping("works", MergeWithLicenses.class.getResourceAsStream("works.json"))
            //.mapping("manifestations", MergeWithLicenses.class.getResourceAsStream("manifestations.json"))
            //.mapping("volumes", MergeWithLicenses.class.getResourceAsStream("volumes.json"))
            ingest.shards(settings.getAsInt("shards", 3))
                    .replica(settings.getAsInt("replica", 0))
                    .newIndex(settings.get("targetCitationIndex"))
                    .startBulk(settings.get("targetCitationIndex"), -1, 1000);
            super.setPipelineProvider(new PipelineProvider<WithCitationsPipeline>() {
                int i = 0;

                @Override
                public WithCitationsPipeline get() {
                    return new WithCitationsPipeline(service, i++);
                }
            });
            super.setConcurrency(settings.getAsInt("concurrency", 1));
            this.prepare();
            this.execute();
            logger.info("shutdown in progress");
            shutdown(new SearchHitPipelineElement().set(null));

            long total = 0L;
            for (WithCitationsPipeline p : getPipelines()) {
                logger.info("pipeline {}, count {}, started {}, ended {}, took {}",
                        p,
                        p.getMetric().count(),
                        DateUtil.formatDateISO(p.getMetric().startedAt()),
                        DateUtil.formatDateISO(p.getMetric().stoppedAt()),
                        TimeValue.timeValueMillis(p.getMetric().elapsed() / 1000000).format());
                total += p.getMetric().count();
            }
            logger.info("total={}", total);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            search.shutdown();
            ingest.shutdown();
        }
    }

    @Override
    public WithCitations execute() {
        super.execute();
        if (logger.isDebugEnabled()) {
            logger.debug("executing");
        }
        boolean failure = false;
        boolean complete = false;

        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setIndices(serialIndex)
                .setTypes(serialType)
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        if (identifier != null) {
            // execute on a single ZDB-ID
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
        }

        // filter only electronic resources?
        //FilterBuilder filterBuilder = FilterBuilders.existsFilter("physicalDescriptionElectronicResource");
        //searchRequest.setFilter(filterBuilder);

        SearchResponse searchResponse = searchRequest.execute().actionGet();
        long total = searchResponse.getHits().getTotalHits();
        long count = 0L;
        long lastpercent = -1L;
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
                    queue().offer(new SearchHitPipelineElement().set(hit), 60, TimeUnit.SECONDS);
                    count++;
                    long percent = count * 100 / total;
                    if (percent != lastpercent && logger.isInfoEnabled()) {
                        logger.info("{}/{} {}% docs={}",
                                count, total, percent,
                                docs.size());
                        for (WithCitationsPipeline p : getPipelines()) {
                            logger.info("{} throughput={} {} {} mean={}",
                                    p.toString(),
                                    p.getMetric().oneMinuteRate(),
                                    p.getMetric().fiveMinuteRate(),
                                    p.getMetric().fifteenMinuteRate(),
                                    p.getMetric().meanRate()
                            );
                        }
                    }
                    lastpercent = percent;
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

    public Client client() {
        return client;
    }

    public Ingest ingest() {
        return ingest;
    }

    public Settings settings() {
        return settings;
    }

    public int size() {
        return size;
    }

    public long millis() {
        return millis;
    }

}
