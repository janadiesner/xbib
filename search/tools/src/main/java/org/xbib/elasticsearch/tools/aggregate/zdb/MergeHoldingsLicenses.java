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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
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

import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.support.client.bulk.BulkClient;
import org.xbib.elasticsearch.support.client.ingest.MockIngestClient;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.BibdatLookup;
import org.xbib.pipeline.QueuePipelineExecutor;
import org.xbib.util.DateUtil;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.ingest.IngestClient;
import org.xbib.elasticsearch.support.client.search.SearchClient;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.Manifestation;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.util.Strings;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.elasticsearch.tools.aggregate.WrappedSearchHit;
import org.xbib.util.ExceptionFormatter;

import static com.google.common.collect.Sets.newSetFromMap;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;

/**
 * Merge ZDB title and holdings and EZB licenses
 */
public class MergeHoldingsLicenses
        extends QueuePipelineExecutor<Boolean, Manifestation, MergeHoldingsLicensesPipeline, WrappedSearchHit> {

    private final static Logger logger = LoggerFactory.getLogger(MergeHoldingsLicenses.class.getSimpleName());

    private final static Set<String> docs = newSetFromMap(new ConcurrentHashMap<String,Boolean>());

    private final static Set<String> clusters = newSetFromMap(new ConcurrentHashMap<String,Boolean>());

    private final static Set<String> manifestations = newSetFromMap(new ConcurrentHashMap<String,Boolean>());

    private MergeHoldingsLicenses service;

    private Client client;

    private Ingest ingest;

    private String sourceTitleIndex;

    private String sourceTitleType;

    private int size;

    private long millis;

    private String identifier;

    private static Settings settings;

    private static BibdatLookup bibdatLookup;

    public static void main(String[] args) {
        try {
            new MergeHoldingsLicenses()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private MergeHoldingsLicenses() {}

    public MergeHoldingsLicenses reader(Reader reader) {
        settings = settingsBuilder().loadFromReader(reader).build();
        return this;
    }

    public MergeHoldingsLicenses settings(Settings newSettings) {
        settings = newSettings;
        return this;
    }

    public MergeHoldingsLicenses writer(Writer writer) {
        return this;
    }

    public MergeHoldingsLicenses run() throws Exception {

        URI sourceURI = URI.create(settings.get("source"));
        this.sourceTitleIndex = settings.get("bibIndex");
        this.sourceTitleType = settings.get("bibType");
        if (Strings.isNullOrEmpty(sourceTitleIndex)) {
            throw new IllegalArgumentException("no bibIndex parameter given");
        }
        if (Strings.isNullOrEmpty(sourceTitleType)) {
            throw new IllegalArgumentException("no bibType parameter given");
        }

        URI targetURI = URI.create(settings.get("target"));

        SearchClient search = new SearchClient().newClient(sourceURI);
        this.service = this;
        this.client = search.client();
        this.size = settings.getAsInt("scrollSize", 10);
        this.millis = settings.getAsTime("scrollTimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
        this.identifier = settings.get("identifier");

        this.ingest = settings.getAsBoolean("mock", false) ?
                new MockIngestClient() :
                "ingest".equals(settings.get("client")) ?
                        new IngestClient() :
                        new BulkClient();

        ingest.maxActionsPerBulkRequest(settings.getAsInt("maxBulkActions", 100))
                .maxConcurrentBulkRequests(settings.getAsInt("maxConcurrentBulkRequests",
                        Runtime.getRuntime().availableProcessors()))
                .setIndex(settings.get("index"))
                .setting(MergeHoldingsLicenses.class.getResourceAsStream("transport-client-settings.json"))
                .newClient(targetURI);
        ingest.waitForCluster();
                        // TODO create settings/mappings
                        //.setting(MergeWithLicenses.class.getResourceAsStream("index-settings.json"))
                        //.mapping("works", MergeWithLicenses.class.getResourceAsStream("works.json"))
                        //.mapping("manifestations", MergeWithLicenses.class.getResourceAsStream("manifestations.json"))
                        //.mapping("volumes", MergeWithLicenses.class.getResourceAsStream("volumes.json"))
        ingest.newIndex()
                .refresh()
                .shards(settings.getAsInt("shards",1))
                .replica(settings.getAsInt("replica",0))
                .startBulk();

        super.provider(new PipelineProvider<MergeHoldingsLicensesPipeline>() {
            int i = 0;

            @Override
            public MergeHoldingsLicensesPipeline get() {
                return new MergeHoldingsLicensesPipeline(service, i++);
            }
        });
        super.concurrency(settings.getAsInt("concurrency", 1));

        this.prepare();
        this.execute();

        logger.info("merge shutdown in progress");
        shutdown(new WrappedSearchHit().set(null));

        for (Pipeline p : getPipelines()) {
            logger.info("pipeline {}, started {}, ended {}, took {}",
                    p,
                    DateUtil.formatDateISO(new Date(p.startedAt())),
                    DateUtil.formatDateISO(new Date(p.stoppedAt())),
                    TimeValue.timeValueMillis(p.took()).format());
        }
        logger.info("ingest shutdown in progress");
        ingest.shutdown();

        logger.info("search shutdown in progress");
        search.shutdown();

        return this;
    }

    @Override
    public MergeHoldingsLicenses prepare() throws IOException {
        super.prepare();
        logger.info("preparing bibdat lookup...");
        bibdatLookup = new BibdatLookup();
        bibdatLookup.buildLookup(client, "bibdat");
        logger.info("bibdat prepared");
        return this;
    }

    @Override
    public MergeHoldingsLicenses execute() throws IOException {
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

    public BibdatLookup bibdatLookup() {
        return bibdatLookup;
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
