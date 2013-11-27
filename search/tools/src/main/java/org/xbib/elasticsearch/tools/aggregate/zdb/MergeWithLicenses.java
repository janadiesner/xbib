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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.SetMultimap;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.IngestClient;
import org.xbib.elasticsearch.support.client.SearchClient;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.date.DateUtil;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.Cluster;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.Indicator;
import org.xbib.util.Strings;
import org.xbib.util.URIUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.elasticsearch.tools.aggregate.WrappedSearchHit;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.Edition;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.Holding;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.License;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.Manifestation;
import org.xbib.elasticsearch.tools.aggregate.zdb.entities.Work;
import org.xbib.options.OptionParser;
import org.xbib.options.OptionSet;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.FormatUtil;


import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Queues.newSynchronousQueue;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Merge ZDB and EZB
 *
 */
public class MergeWithLicenses {

    private final static Logger logger = LoggerFactory.getLogger(MergeWithLicenses.class.getName());

    private final static Logger missingRelationsLogger = LoggerFactory.getLogger("relations");

    private final static String lf = System.getProperty("line.separator");

    private final static int currentYear = DateUtil.getYear();

    // the pump
    private int numPumps;
    private BlockingQueue<WrappedSearchHit> pumpQueue;
    private ExecutorService pumpService;
    private CountDownLatch pumpLatch;
    private Set<MergePump> pumps;

    private Client client;
    private Ingest ingest;

    // Elasticsearch source index/types
    private String sourceTitleIndex;
    private String sourceTitleType;
    private String sourceHoldingsIndex;
    private String sourceHoldingsType;
    private String sourceLicenseIndex;
    private String sourceLicenseType;
    private String sourceIndicatorIndex;
    private String sourceIndicatorType;

    // Elastiscearch target index/type
    private String targetIndex;
    private String targetWorksType;
    private String targetManifestationsType;

    private int size;
    private long millis;

    private String identifier;

    // counters
    private final static AtomicLong queryCounter = new AtomicLong(0L);
    private final static AtomicLong hitCounter = new AtomicLong(0L);
    private final static AtomicLong writeCounter = new AtomicLong(0L);
    private final static AtomicLong manifestationCounter = new AtomicLong(0L);
    private final static AtomicLong holdingCounter = new AtomicLong(0L);
    private final static AtomicLong licenseCounter = new AtomicLong(0L);
    private final static AtomicLong indicatorCounter = new AtomicLong(0L);

    private Set<String> docs;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("source").withRequiredArg().ofType(String.class).required();
                    accepts("target").withRequiredArg().ofType(String.class).required();
                    accepts("shards").withOptionalArg().ofType(Integer.class).defaultsTo(4);
                    accepts("replica").withOptionalArg().ofType(Integer.class).defaultsTo(0);
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors() * 4);
                    accepts("pumps").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors() * 4);
                    accepts("size").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("millis").withRequiredArg().ofType(Long.class).defaultsTo(60000L);
                    accepts("id").withOptionalArg().ofType(String.class);
                }
            };
            OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + MergeWithLicenses.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --source <uri>         URI for connecting to the Elasticsearch source" + lf
                        + " --target <uri>         URI for connecting to Elasticsearch target" + lf
                        + " --shards <n>           number of shards" + lf
                        + " --replica <n>          number of replica" + lf
                        + " --mock <bool>          dry run of indexing (optional, default: false)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 1000)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: number of cpu core * 4)"
                        + " --pumps <n>            number of pumps (optional, default: number of cpu cores * 4)"
                        + " --size <n>             size for scan query result (optional, default: 1000)"
                        + " --millis <ms>          life time in milliseconds for scan query (optional, default: 60000)"
                        + " --id <n>               ZDB ID (optional, default is all ZDB IDs)"
                );
                System.exit(1);
            }

            URI sourceURI = URI.create((String)options.valueOf("source"));
            URI targetURI = URI.create((String)options.valueOf("target"));
            Integer maxBulkActions = (Integer) options.valueOf("maxbulkactions");
            Integer maxConcurrentBulkRequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            Integer shards = (Integer)options.valueOf("shards");
            Integer pumps = (Integer) options.valueOf("pumps");
            Integer size = (Integer) options.valueOf("size");
            Long millis = (Long) options.valueOf("millis");
            String identifier = (String) options.valueOf("id");

            logger.info("connecting to search source {}...", sourceURI);

            SearchClient search = new SearchClient()
                    .newClient(sourceURI);

            logger.info("connecting to target index {} ...", targetURI);

            Ingest ingest = new IngestClient()
                    .maxBulkActions(maxBulkActions)
                    .maxConcurrentBulkRequests(maxConcurrentBulkRequests)
                    .newClient(targetURI)
                    .waitForCluster()
                    .setIndex(URIUtil.parseQueryString(targetURI).get("index"))
                    .setting(MergeWithLicenses.class.getResourceAsStream("settings.json"))
                    .addMapping("works", MergeWithLicenses.class.getResourceAsStream("works.json"))
                    .addMapping("manifestations", MergeWithLicenses.class.getResourceAsStream("manifestations.json"))
                    .newIndex()
                    .refresh()
                    .shards(shards)
                    .replica(0)
                    .startBulk();

            logger.info("starting aggregation: pumps={} size={}, millis={}", pumps, size, millis);
            long t0 = System.currentTimeMillis();
            new MergeWithLicenses(search, ingest, sourceURI, targetURI, pumps, size, millis, identifier)
                    .aggregate();
            long t1 = System.currentTimeMillis();

            long d = writeCounter.get(); //number of documents written
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
                    indicatorCounter.get());


            ingest.shutdown();
            search.shutdown();


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private MergeWithLicenses(SearchClient search, Ingest ingest,
                             URI sourceURI, URI targetURI,
                             int numPumps, int size, long millis,
                             String identifier)
            throws UnsupportedEncodingException {
        this.client = search.client();
        this.ingest = ingest;

        Map<String,String> params = URIUtil.parseQueryString(sourceURI);

        // ZDB BIB
        this.sourceTitleIndex = params.get("bibIndex");
        this.sourceTitleType = params.get("bibType");
        if (Strings.isNullOrEmpty(sourceTitleIndex)) {
            throw new IllegalArgumentException("no bibIndex parameter given");
        }
        if (Strings.isNullOrEmpty(sourceTitleType)) {
            throw new IllegalArgumentException("no bibType parameter given");
        }
        // ZDB HOL
        this.sourceHoldingsIndex = params.get("holIndex");
        this.sourceHoldingsType = params.get("holType");
        if (Strings.isNullOrEmpty(sourceHoldingsIndex)) {
            throw new IllegalArgumentException("no holIndex parameter given");
        }
        if (Strings.isNullOrEmpty(sourceHoldingsType)) {
            throw new IllegalArgumentException("no holType parameter given");
        }
        // EZB licenses
        this.sourceLicenseIndex = params.get("licenseIndex");
        this.sourceLicenseType = params.get("licenseType");
        if (Strings.isNullOrEmpty(sourceLicenseIndex)) {
            throw new IllegalArgumentException("no licenseIndex parameter given");
        }
        if (Strings.isNullOrEmpty(sourceLicenseType)) {
            throw new IllegalArgumentException("no licenseType parameter given");
        }
        // EZB indicators from web service
        this.sourceIndicatorIndex = params.get("indicatorIndex");
        this.sourceIndicatorType = params.get("indicatorType");
        if (Strings.isNullOrEmpty(sourceIndicatorIndex)) {
            throw new IllegalArgumentException("no indicatorIndex parameter given");
        }
        if (Strings.isNullOrEmpty(sourceIndicatorType)) {
            throw new IllegalArgumentException("no indicatorType parameter given");
        }

        params = URIUtil.parseQueryString(targetURI);

        this.targetIndex = params.get("index");
        if (targetIndex == null) {
            targetIndex = sourceTitleIndex + "merge";
        }
        this.targetWorksType = "works";
        this.targetManifestationsType = "manifestations";

        this.size = size;
        this.millis = millis;

        this.docs =  Sets.newConcurrentHashSet();  //Collections.synchronizedSet(new HashSet());

        this.identifier = identifier;

        this.numPumps = numPumps;
        this.pumps = newHashSet();
        this.pumpQueue = newSynchronousQueue();
        this.pumpService = Executors.newFixedThreadPool(numPumps);
        this.pumpLatch = new CountDownLatch(numPumps);
        for (int i = 0; i < numPumps; i++) {
            MergePump mergePump = new MergePump(i);
            pumps.add(mergePump);
            pumpService.submit(mergePump);
        }
    }

    private MergeWithLicenses aggregate() {

        boolean failure = false;

        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        if (identifier != null) {
            // ZDB-ID
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("aggregate request = {}", searchRequest.toString());
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        if (logger.isDebugEnabled()) {
            logger.debug("hits={}", searchResponse.getHits().getTotalHits());
        }
        while (!failure && searchResponse.getScrollId() != null) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                try {
                    WrappedSearchHit w = new WrappedSearchHit(hit);
                    pumpQueue.put(w);
                } catch (InterruptedException e) {
                    logger.error("interrupted");
                    Thread.currentThread().interrupt();
                    failure = true;
                } catch (Throwable e) {
                    logger.error("error while pumping, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    Thread.currentThread().interrupt();
                }
            }
        }
        logger.info("send 'end of pump'");
        for (int i = 0; i < numPumps; i++) {
            try {
                // poison element
                pumpQueue.put(new WrappedSearchHit(null));
            } catch (InterruptedException e) {
                logger.error("interrupted");
            }
        }

        logger.info("waiting for pumps...");
        try {
            pumpLatch.await();
        } catch (InterruptedException e) {
            logger.error("interrupted");
        }

        logger.info("end of aggregation, failure = {}, shutting down...", failure);

        pumpService.shutdownNow();

        return this;
    }

    class MergePump implements Callable<Boolean> {

        private final Logger logger;

        private final ObjectMapper mapper;

        private final Queue<ClusterBuildContinuation> buildQueue;

        private Set<Manifestation> cluster;

        public MergePump(int i) {
            this.buildQueue = new ConcurrentLinkedQueue();
            this.logger = LoggerFactory.getLogger(MergeWithLicenses.class.getName() + "-pump-" + i);
            this.mapper = new ObjectMapper();
        }

        public Queue<ClusterBuildContinuation> getBuildQueue() {
            return buildQueue;
        }

        public Collection<Manifestation> getCluster() {
            return cluster;
        }

        @Override
        public Boolean call() throws Exception {
            Manifestation m = null;
            try {
                long count = 0;
                while (true) {
                    WrappedSearchHit t = pumpQueue.take();
                    if (t.hit() == null) {
                        logger.info("received 'end of pump' message");
                        break;
                    }
                    m = new Manifestation(mapper.readValue(t.hit().source(), Map.class));
                    //if (filterForProcess(m)) {
                        process(m);
                    //} else {
                    //    logger.debug("filtered, not processed: {}", m);
                    //}
                    count++;
                    if (count % size == 0) {
                         logger.info("count={} queries={} hits={} manifestations={} holdings={} licences={}",
                                 count,
                                 queryCounter.get(),
                                 hitCounter.get(),
                                 manifestationCounter.get(),
                                 holdingCounter.get(),
                                 indicatorCounter.get());
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("pump processing interrupted");
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                logger.error("exception while processing {}, exiting", m);
                logger.error(ExceptionFormatter.format(e));
                Thread.currentThread().interrupt();
            } finally {
                pumpLatch.countDown();
            }
            logger.info("pump processing terminating");
            return true;
        }

        private Set<Manifestation> used = newConcurrentHashSet();

        private void logManifestation(Manifestation m, String relation, int indent) {
            logger.info("{} {} {} {} {}-{} {} {} {} {}",
                    indent,
                    relation,
                    m.externalID(),
                    m.title(),
                    m.firstDate(), m.lastDate(),
                    m.country(),
                    m.contentType() + "/" + m.mediaType() + "/" + m.carrierType(),
                    m.isPartial() ? "(Unterreihe)" : "",
                    m.isSupplement() ? "(Suppl.)" : "");
            if (used.contains(m)) {
                // loop detected
                return;
            }
            used.add(m);
            Set<String> relations = m.getRelatedManifestations().keySet();
            for (String rel : relations) {
                if (Manifestation.carrierEditions().contains(rel)
                        || "hasPart".equals(rel) || "hasSupplement".equals(rel)) {
                    for (Manifestation mm : m.getRelatedManifestations().get(rel)) {
                        logManifestation(mm, rel, indent+1);
                    }
                }
            }
        }

        private void process(Manifestation manifestation) throws IOException {
            if (docs.contains(manifestation.externalID())) {
                return;
            }
            docs.add(manifestation.externalID());

            this.cluster = newTreeSet(Manifestation.getIdComparator());
            cluster.add(manifestation);
            // supplements: do not build a cluster for them.
            if (!manifestation.isSupplement()) {
                retrieveCluster(cluster, manifestation);
            }
            manifestationCounter.addAndGet(cluster.size());

            // update all relations between manifestations in the cluster
            for (Manifestation m : cluster) {
                setAllRelationsBetween(m, cluster);
            }

            // build cluster
            Cluster c = new Cluster(cluster);
            List<Set<Manifestation>> chronoStreams = c.chronoStreams();

            used.clear();
            for (Set<Manifestation> chronoStream : chronoStreams) {
                // per chrono stream, find holdings, licenses, indicators
                Set<Holding> holdings = Sets.newHashSet();
                searchHoldings(chronoStream, holdings);
                if (logger.isDebugEnabled()) {
                    logger.debug("found {} holdings for manifestations {}",
                            holdings.size(), chronoStream);
                }
                Set<License> licenses = Sets.newHashSet();
                searchLicenses(chronoStream, licenses);
                if (logger.isDebugEnabled()) {
                    logger.debug("found {} licenses for manifestations {}",
                            licenses.size(), chronoStream);
                }
                Set<Indicator> indicators = Sets.newHashSet();
                searchIndicators(chronoStream, indicators);
                if (logger.isDebugEnabled()) {
                    logger.debug("found {} indicators for manifestations {}",
                            indicators.size(), chronoStream);
                }

                // bring date order into holdings, licenses, indicators
                Map<Integer, Set<Holding>> holdingsByDate = reorderHoldingsByDate(holdings);
                Map<Integer, Set<License>> licensesByDate = reorderLicensesByDate(licenses);
                Map<Integer, Set<Indicator>> indicatorByDate = reorderIndicatorsByDate(indicators);

                logger.info("{} {} {}", holdingsByDate.size(), licensesByDate.size(), indicatorByDate.size());

                // dump chrono stream
                logger.info("----");
                for (Manifestation m : chronoStream) {
                    logManifestation(m, "", 0);
                }
            }

            logger.info("docs = {}", docs.size());

            logger.info("used = {}", used.size());

            /*Set<Work> leaders = electWorkLeaders(cluster);
            if (logger.isDebugEnabled()) {
                logger.debug("elected {} leaders from cluster {} -> {}",
                        leaders.size(), cluster, leaders);
            }*/

            // try to assign all other manifestations in the cluster to one of the leaders
            /*boolean ready = false;
            while (!ready) {
                Collection<Manifestation> manifestations = new ArrayList(cluster); // must be a list!
                boolean found = false;
                for (Manifestation m : manifestations) {
                    for (Work w : leaders) {
                        if (isConnected(m, w.getManifestations())) {
                            w.addManifestation(m);
                            cluster.remove(m);
                            found = true;
                            break;
                        }
                    }
                }
                ready = cluster.isEmpty() || !found;
            }
            if (!cluster.isEmpty()) {
                logger.error("cluster not empty?!? {} leaders found so far = {}", cluster, leaders);
                // promote orphaned manifestations in the cluster to works.
                for (Manifestation m : cluster) {
                    leaders.add(new Work(m));
                }
            }*/

            Set<Work> leaders = Sets.newHashSet();
            for (Work work : leaders) {
                Set<Holding> holdings = new HashSet();
                Set<License> licenses = new HashSet();
                Map<String,Edition> editions = electEditionLeaders(work.getManifestations());
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: found {} editions: {}", work, editions.size(), editions);
                }
                Set<Edition> sortedExpr = new TreeSet(Edition.getCurrentComparator());
                sortedExpr.addAll(editions.values());
                work.setEditions(sortedExpr);
                // search for all holdings of all manifestations in the cluster with one query
                Set<String> manifestationsIDs = work.allIDs();
                Set<String> manifestationsTargetIDs = work.allTargetIDs();
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: searching holdings for manifestations {}", work, manifestationsTargetIDs);
                }
                searchHoldings(manifestationsIDs, holdings);
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: found {} holdings ", work, holdings.size());
                }
                holdingCounter.addAndGet(holdings.size());
                // search for license documents
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: searching licenses for manifestations {}", work, manifestationsTargetIDs);
                }
                searchLicenses(manifestationsTargetIDs, licenses);
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: found {} licenses ", work, licenses.size());
                }
                indicatorCounter.addAndGet(licenses.size());
                // output phase, write everything out
                if (logger.isDebugEnabled()) {
                    logger.debug("writing work {} title '{}'", work, work.title());
                }
                Map<Integer, Set<Holding>> holdingsByDate = reorderHoldingsByDate(holdings);
                Map<Integer, Set<License>> licensesByDate = reorderLicensesByDate(licenses);
                // compute static boost for work
                TreeSet<Integer> dates = new TreeSet(holdingsByDate.keySet());
                dates.addAll(licensesByDate.keySet());

                double boost = 1.0;
                boost += dates.size();
                for (Integer d : dates) {
                    boost += 0.1 * d * (1 +
                            (holdingsByDate.containsKey(d) ? holdingsByDate.get(d).size() : 0) +
                            (licensesByDate.containsKey(d) ? licensesByDate.get(d).size() : 0));
                }

                writeWork(work, manifestationsTargetIDs, holdings, licenses, boost,
                         dates.isEmpty() ? null : dates.first(),
                         dates.isEmpty() ? null : dates.last());
                writeManifestations(work.getManifestations(), dates, holdingsByDate, licensesByDate);
            }
        }

        private void retrieveCluster(Collection<Manifestation> cluster,
                Manifestation manifestation) throws IOException {
            SetMultimap<String, String> relations = manifestation.relations();
            Set<String> neighbors =  Sets.newHashSet(relations.values());
            QueryBuilder queryBuilder = neighbors.isEmpty() ?
                    termQuery("_all",  manifestation.id()) :
                    boolQuery().should(termQuery("_all",  manifestation.id()))
                            .should(termsQuery("IdentifierDNB.identifierDNB", neighbors.toArray()));
            SearchRequestBuilder searchRequest = client.prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(size) // size is per shard!
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(millis));
            searchRequest.setIndices(sourceTitleIndex);
            if (sourceTitleType != null) {
                searchRequest.setTypes(sourceTitleType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                   .execute().actionGet();
            queryCounter.incrementAndGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                return;
            }
            ClusterBuildContinuation cont =
                    new ClusterBuildContinuation(manifestation, searchResponse, 0, cluster);
            buildQueue.offer(cont);
            while (!buildQueue.isEmpty()) {
                continueClusterBuild(buildQueue.poll());
            }
        }

        private void continueClusterBuild(ClusterBuildContinuation c) throws IOException {
            SearchResponse searchResponse = c.searchResponse;
            SearchHits hits;
            do {
                hits = searchResponse.getHits();
                for (int i = c.pos; i < hits.getHits().length; i++ ) {
                    SearchHit hit = hits.getAt(i);
                    hitCounter.incrementAndGet();
                    Manifestation m = new Manifestation(mapper.readValue(hit.source(), Map.class));
                    boolean collided = detectCollisionAndTransfer(m, c, i);
                    if (collided) {
                        return;
                    }
                    // global docs
                    if (docs.contains(m.externalID())) {
                        continue;
                    }
                    docs.add(m.externalID());
                    // check for local cluster docs (avoid loops)
                    if (c.cluster.contains(m)) {
                        continue;
                    }
                    c.cluster.add(m);
                    boolean temporalRelation = false;
                    boolean carrierRelation = false;
                    for (String relation : findTheRelationsBetween(c.manifestation, m.id())) {
                        if (relation == null) {
                            logger.warn("unknown relation {}", relation);
                            continue;
                        }
                        c.manifestation.addRelatedManifestation(relation, m);
                        String inverse = inverseRelations.get(relation);
                        if (inverse == null) {
                            logger.warn("no inverse relation for {}", relation);
                        } else {
                            m.addRelatedManifestation(inverse, c.manifestation);
                        }
                        temporalRelation = temporalRelation
                                || "precededBy".equals(relation)
                                || "succeededBy".equals(relation);
                        carrierRelation = carrierRelation
                                || Manifestation.carrierEditions().contains(relation);
                    }
                    // other direction (missing entries in catalog are possible)
                    for (String relation : findTheRelationsBetween(m, c.manifestation.id())) {
                        if (relation == null) {
                            logger.warn("unknown relation {}", relation);
                            continue;
                        }
                        m.addRelatedManifestation(relation, c.manifestation);
                        String inverse = inverseRelations.get(relation);
                        if (inverse == null) {
                            logger.warn("no inverse relation for {}", relation);
                        } else {
                            c.manifestation.addRelatedManifestation(inverse, m);
                        }
                        temporalRelation = temporalRelation
                                || "precededBy".equals(relation)
                                || "succeededBy".equals(relation);
                        carrierRelation = carrierRelation
                                || Manifestation.carrierEditions().contains(relation);
                    }
                    // expand cluster for this manifestation iff temporal or carrier relation
                    // also expand if there are any other print/online editions
                    if (temporalRelation
                            || carrierRelation
                            || m.hasCarrierRelations()) {
                        retrieveCluster(c.cluster, m);
                    }
                }
                searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(millis))
                        .execute().actionGet();
                queryCounter.incrementAndGet();
                hits = searchResponse.getHits();
            } while (hits.getHits().length > 0);
        }

        private boolean detectCollisionAndTransfer(Manifestation manifestation,
                                        ClusterBuildContinuation c, int pos) {
            for (MergePump pump : pumps) {
                if (this == pump) {
                    continue;
                }
                if (pump.getCluster().contains(manifestation)) {
                    logger.warn("collision detected for {} at hit pos {}", manifestation, pos);
                    c.pos = pos;
                    pump.getBuildQueue().offer(c);
                    return true;
                }
            }
            return false;
        }

        private void searchHoldings(Collection<Manifestation> manifestations, Set<Holding> holdings) throws IOException {
            Set<String> ids = Sets.newHashSet();
            for (Manifestation m : manifestations) {
                ids.add(m.externalID());
                if (m.getPrintExternalID() != null) {
                    ids.add(m.getPrintExternalID());
                }
            }
            searchHoldings(ids, holdings);
        }

        private void searchHoldings(Set<String> ids, Set<Holding> holdings) throws IOException {
            if (ids == null || ids.isEmpty()) {
                return;
            }
            // split ids into portions of 1024 (default max clauses for Lucene)
            Object[] array = ids.toArray();
            for (int begin = 0; begin < array.length; begin += 1024) {
                int end = begin + 1024 > array.length ? array.length : begin + 1024;
                Object[] subarray = Arrays.copyOfRange(array, begin, end);
                QueryBuilder queryBuilder = termsQuery("_all", subarray);
                // size is per shard
                SearchRequestBuilder searchRequest = client.prepareSearch()
                        .setQuery(queryBuilder)
                        .setSize(size)
                        .setSearchType(SearchType.SCAN)
                        .setScroll(TimeValue.timeValueMillis(millis));
                searchRequest.setIndices(sourceHoldingsIndex);
                if (sourceHoldingsType != null) {
                    searchRequest.setTypes(sourceHoldingsType);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("searchHoldings request = {}", searchRequest.toString());
                }
                SearchResponse searchResponse = searchRequest.execute().actionGet();
                if (logger.isDebugEnabled()) {
                    logger.debug("hits = {}", searchResponse.getHits().getTotalHits());
                }
                while (searchResponse.getScrollId() != null) {
                    searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                            .setScroll(TimeValue.timeValueMillis(millis))
                            .execute().actionGet();
                    SearchHits hits = searchResponse.getHits();
                    if (hits.getHits().length == 0) {
                        break;
                    }
                    queryCounter.incrementAndGet();
                    for (SearchHit hit : hits) {
                        hitCounter.incrementAndGet();
                        Holding holding = new Holding(mapper.readValue(hit.source(), Map.class));
                        holdings.add(holding);
                    }
                }
            }
        }

        private void searchLicenses(Collection<Manifestation> manifestations, Set<License> licenses) throws IOException {
            Set<String> ids = Sets.newHashSet();
            for (Manifestation m : manifestations) {
                ids.add(m.externalID());
                if (m.getOnlineExternalID() != null) {
                    ids.add(m.getOnlineExternalID());
                }
            }
            searchLicenses(ids, licenses);
        }

        private void searchLicenses(Set<String> ids, Set<License> licenses) throws IOException {
            if (ids == null || ids.isEmpty()) {
                return;
            }
            // split ids into portions of 1024 (default max clauses for Lucene)
            Object[] array = ids.toArray();
            for (int begin = 0; begin < array.length; begin += 1024) {
                int end = begin + 1024 > array.length ? array.length : begin + 1024;
                Object[] subarray = Arrays.copyOfRange(array, begin, end);
                QueryBuilder queryBuilder = termsQuery("ezb:zdbid", subarray);
                // size is per shard
                SearchRequestBuilder searchRequest = client.prepareSearch()
                        .setQuery(queryBuilder)
                        .setSize(size)
                        .setSearchType(SearchType.SCAN)
                        .setScroll(TimeValue.timeValueMillis(millis));
                searchRequest.setIndices(sourceLicenseIndex);
                if (sourceLicenseType != null) {
                    searchRequest.setTypes(sourceLicenseType);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("searchLicenses request = {}", searchRequest.toString());
                }
                SearchResponse searchResponse = searchRequest.execute().actionGet();
                if (logger.isDebugEnabled()) {
                    logger.debug("hits = {}", searchResponse.getHits().getTotalHits());
                }
                while (true) {
                    searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                            .setScroll(TimeValue.timeValueMillis(millis))
                            .execute().actionGet();
                    SearchHits hits = searchResponse.getHits();
                    if (hits.getHits().length == 0) {
                        break;
                    }
                    queryCounter.incrementAndGet();
                    for (SearchHit hit : hits) {
                        hitCounter.incrementAndGet();
                        License license = new License(mapper.readValue(hit.source(), Map.class));
                        if (license.getISIL().startsWith("DE-")) {
                            licenses.add(license);
                        }
                    }
                }
            }
        }

        private void searchIndicators(Collection<Manifestation> manifestations, Set<Indicator> indicators) throws IOException {
            Set<String> ids = Sets.newHashSet();
            for (Manifestation m : manifestations) {
                ids.add(m.externalID());
                if (m.getOnlineExternalID() != null) {
                    ids.add(m.getOnlineExternalID());
                }
            }
            searchIndicators(ids, indicators);
        }

        private void searchIndicators(Set<String> ids, Set<Indicator> indicators) throws IOException {
            if (ids == null || ids.isEmpty()) {
                return;
            }
            // split ids into portions of 1024 (default max clauses for Lucene)
            Object[] array = ids.toArray();
            for (int begin = 0; begin < array.length; begin += 1024) {
                int end = begin + 1024 > array.length ? array.length : begin + 1024;
                Object[] subarray = Arrays.copyOfRange(array, begin, end);
                QueryBuilder queryBuilder = termsQuery("xbib:identifier", subarray);
                // size is per shard
                SearchRequestBuilder searchRequest = client.prepareSearch()
                        .setQuery(queryBuilder)
                        .setSize(size)
                        .setSearchType(SearchType.SCAN)
                        .setScroll(TimeValue.timeValueMillis(millis));
                searchRequest.setIndices(sourceIndicatorIndex);
                if (sourceLicenseType != null) {
                    searchRequest.setTypes(sourceIndicatorType);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("searchIndicators request = {}", searchRequest.toString());
                }
                SearchResponse searchResponse = searchRequest.execute().actionGet();
                if (logger.isDebugEnabled()) {
                    logger.debug("hits = {}", searchResponse.getHits().getTotalHits());
                }
                while (true) {
                    searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                            .setScroll(TimeValue.timeValueMillis(millis))
                            .execute().actionGet();
                    SearchHits hits = searchResponse.getHits();
                    if (hits.getHits().length == 0) {
                        break;
                    }
                    queryCounter.incrementAndGet();
                    for (SearchHit hit : hits) {
                        hitCounter.incrementAndGet();
                        Indicator indicator = new Indicator(mapper.readValue(hit.source(), Map.class));
                        if (indicator.getISIL().startsWith("DE-")) {
                            indicators.add(indicator);
                        }
                    }
                }
            }
        }
    }

    private Set<String> findTheRelationsBetween(Manifestation manifestation, String id) {
        if (manifestation.id().equals(id)) {
            return null;
        }
        Set<String> relationNames = new HashSet<String>();
        for (String entry : Manifestation.relationEntries()) {
            String s = checkEntry(manifestation.map().get(entry), id);
            if (s != null) {
                relationNames.add(s);
            }
        }
        return relationNames;
    }

    private String checkEntry(Object o, String id) {
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            for (Object s : (List) o) {
                Map<String, Object> entry = (Map<String, Object>) s;
                String value = (String)entry.get("identifierDNB");
                if (id.equals(value)) {
                    return (String)entry.get("relation");
                }
            }
        }
        return null;
    }

    private void setAllRelationsBetween(Manifestation manifestation, Collection<Manifestation> cluster) {
        for (String relation : Manifestation.relationEntries()) {
            Object o = manifestation.map().get(relation);
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
                }
                for (Object s : (List) o) {
                    Map<String, Object> entry = (Map<String, Object>) s;
                    String key = (String)entry.get("relation");
                    if (key == null) {
                        logger.warn("entry {} has no relation name in {}", entry, manifestation.externalID());
                        continue;
                    }
                    String value = (String)entry.get("identifierDNB");
                    for (Manifestation m : cluster) {
                        if (m.id().equals(manifestation.id())) {
                            continue;
                        }
                        if (m.id().equals(value)) {
                            manifestation.addRelatedManifestation(key, m);
                            String inverse = inverseRelations.get(key);
                            if (inverse == null) {
                                logger.warn("no inverse relation for {} in {}", key, manifestation.externalID());
                            } else {
                                m.addRelatedManifestation(inverse, manifestation);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, Edition> electEditionLeaders(Set<Manifestation> manifestations) {
        Map<String, Edition> m = new TreeMap();
        for (Manifestation manifestation : manifestations) {
            String editionKey = manifestation.country().toString();
            Edition edition = m.get(editionKey);
            if (edition == null) {
                edition = new Edition(editionKey, manifestation);
                m.put(editionKey, edition);
            }
            edition.addManifestation(manifestation);
        }
        return m;
    }

    private Map<Integer, Set<Holding>> reorderHoldingsByDate(Set<Holding> holdings) {
        Map<Integer, Set<Holding>> holdingsByDate = newTreeMap();
        for (Holding holding : holdings) {
            List<Integer> dates = null;
            // first, our dates!
            Object o = holding.map().get("dates");
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
                }
                dates = newLinkedList();
                dates.addAll((List<Integer>)o);
            } else {
                o = holding.map().get("FormattedEnumerationAndChronology");
                if (o != null) {
                    if (!(o instanceof List)) {
                        o = Arrays.asList(o);
                    }
                    dates = parseDates((List<Map<String, Object>>) o);
                } else {
                    o = holding.map().get("NormalizedHolding");
                    if (o != null) {
                        if (!(o instanceof List)) {
                            o = Arrays.asList(o);
                        }
                        dates = parseDates((List<Map<String, Object>>) o);
                    }
                }
            }
            if (dates == null || dates.isEmpty()) {
                continue;
            }
            for (Integer date : dates) {
                Set<Holding> set = holdingsByDate.get(date);
                if (set == null) {
                    set = new HashSet();
                }
                set.add(holding);
                holdingsByDate.put(date, set);
            }
        }
        return holdingsByDate;
    }

    private List<Integer> parseDates(List<Map<String, Object>> groups) {
        int[] begin = new int[groups.size()];
        int[] end = new int[groups.size()];
        int max = 0;
        for (Map<String, Object> m : groups) {
            Object o = m.get("movingwall");
            if (o != null) {
                logger.warn("movingwall detected: {}", o);
            }
            o = m.get("date");
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            for (String date : (List<String>) o) {
                if (date == null) {
                    continue;
                }
                date = date.replaceAll("[^\\d]", "");
                if (date.length() < 4) {
                    continue;
                }
                if (date.length() > 4) {
                    date = date.substring(0, 4);
                }
                int d = Integer.parseInt(date);
                if (d < 1500) {
                    d = 1500;
                }
                if (d > currentYear) {
                    d = currentYear;
                }
                String number = (String) m.get("sequenceNumber");
                if (number == null) {
                    continue;
                }
                int pos = number.indexOf('.');
                int p = pos >= 0 ? Integer.parseInt(number.substring(0, pos)) - 1 : 0;
                int q = pos >= 0 ? Integer.parseInt(number.substring(pos + 1, pos + 2)) : 0;
                if (q == 1) {
                    begin[p] = d;
                } else if (q == 2) {
                    end[p] = d;
                }
                if (p > max) {
                    max = p;
                }
            }
        }
        List<Integer> dates = newLinkedList();
        for (int i = 0; i < max+1; i++) {
            if (begin[i] > 0 && end[i] > 0) {
                for (int d = begin[i]; d <= end[i]; d++) {
                    dates.add(d);
                }
            } else if (begin[i] > 0) {
                dates.add(begin[i]);
            }
        }
        return dates;
    }

    private Map<Integer, Set<License>> reorderLicensesByDate(Set<License> licenses) {
        Map<Integer, Set<License>> licensesByDate = newTreeMap();
        for (License license : licenses) {
            for (Integer d : license.dates()) {
                Set<License> l = licensesByDate.get(d);
                if (l == null) {
                    l = newHashSet();
                }
                l.add(license);
                licensesByDate.put(d, l);
            }
        }
        return licensesByDate;
    }

    private Map<Integer, Set<Indicator>> reorderIndicatorsByDate(Set<Indicator> indicators) {
        Map<Integer, Set<Indicator>> indicatorsByDate = newTreeMap();
        for (Indicator indicator : indicators) {
            for (Integer d : indicator.dates()) {
                Set<Indicator> l = indicatorsByDate.get(d);
                if (l == null) {
                    l = newHashSet();
                }
                l.add(indicator);
                indicatorsByDate.put(d, l);
            }
        }
        return indicatorsByDate;
    }

    private void writeWork(Work work,
                           Set<String> manifestationIDs,
                           Set<Holding> holdings,
                           Set<License> licenses,
                           double boost,
                           Integer firstDate,
                           Integer lastDate)
            throws IOException {
        if (work == null) {
            return;
        }
        XContentBuilder builder = jsonBuilder();
        List<String> editionIDs = newLinkedList();
        for (Edition expr : work.getEditions()) {
            editionIDs.add(expr.externalID());
        }
        // compute ISIL set
        Set<String> isils = newHashSet();
        for (Holding holding : holdings) {
            if (holding.getISIL() != null) {
                isils.add(holding.getISIL());
            }
        }
        for (License license : licenses) {
            if (license.getISIL() != null) {
                isils.add(license.getISIL());
            }
        }

        builder.startObject()
                .field("_boost", boost)
                .field("id", work.externalID())
                .field("title", work.title())
                .field("key", work.getUniqueIdentifier())
                .field("firstDate", firstDate)
                .field("lastDate", lastDate)
                .field("editionCount", editionIDs.size())
                .field("manifestationCount", manifestationIDs.size())
                .field("isilCount", isils.size())
                .field("hasEditions", editionIDs)
                .field("hasManifestation", manifestationIDs)
                .field("hasISIL", isils);
        if (work.isSupplement()) {
            builder.field("hasSupplement", work.supplementExternalID());
        }
        Set<Manifestation> manifestations = new TreeSet(work.getManifestations());
        builder.startArray("editions");
        for (Edition edition : work.getEditions()) {
            builder.startObject()
                    .field("id", edition.externalID())
                    .field("country", edition.country())
                    .field("manifestationCount", edition.getManifestations().size())
                    .startArray("manifestations");
            for (Manifestation manifestation : edition.getManifestations()) {
                writeManifestation(builder, manifestation);
            }
            manifestations.removeAll(edition.getManifestations());
            builder.endArray();
            builder.endObject();
        }
        // last, write manifestations without edition
        if (!manifestations.isEmpty()) {
            builder.startObject()
                    .field("id", (String)null)
                    .field("manifestationCount", manifestations.size())
                    .startArray("manifestations");
            for (Manifestation manifestation : manifestations) {
                writeManifestation(builder, manifestation);
            }
            builder.endArray()
                    .endObject();
        }
        builder.endArray()
             .endObject();
        ingest.indexDocument(targetIndex,
                targetWorksType,
                work.externalID(),
                builder.string());
        writeCounter.incrementAndGet();
    }

    private void writeManifestation(XContentBuilder builder, Manifestation manifestation) throws IOException {
        builder.startObject()
                .field("id", manifestation.externalID())
                .field("title", manifestation.cleanTitle())
                .field("publisher", manifestation.publisher())
                .field("publisherPlace", manifestation.publisherPlace())
                .field("country", manifestation.country())
                .field("language", manifestation.language())
                .field("identifiers", manifestation.getIdentifiers())
                .field("firstDate", manifestation.firstDate())
                .field("lastDate", manifestation.lastDate())
                .field("contentType", manifestation.contentType())
                .field("mediaType", manifestation.mediaType())
                .field("carrierType", manifestation.carrierType());
        if (manifestation.hasOnline()) {
            builder.field("hasOnline", manifestation.getOnlineExternalID());
        }
        if (manifestation.hasPrint()) {
            builder.field("hasPrint", manifestation.getPrintExternalID());
        }
        if (manifestation.isSupplement()) {
            builder.field("hasSupplement", manifestation.supplementExternalID());
        }
        builder.endObject();
    }

    private void writeManifestations(Set<Manifestation> manifestations,
                                    Set<Integer> dates,
                                    Map<Integer, Set<Holding>> holdingsByDate,
                                    Map<Integer, Set<License>> licensesByDate)
         throws IOException {
        if (manifestations.isEmpty()) {
            return;
        }
        if (dates.isEmpty()) {
            return;
        }
        // build a "cross reference map" for manifestations
        Map<String,Manifestation> map = new HashMap();
        for (Manifestation manifestation : manifestations) {
            map.put(manifestation.id(), manifestation);
            map.put(manifestation.externalID(), manifestation);
        }
        for (Manifestation manifestation : manifestations) {
            String mid = manifestation.id();
            String eid = manifestation.externalID();
            String pid = manifestation.getPrintID();
            String oid = manifestation.getOnlineExternalID();
            // Loop over dates and find out if there are services for this date offered by libraries.
            List<Integer> foundDates = new ArrayList();
            for (Integer date : dates) {
                Map<String,List<Holding>> services = new HashMap();
                // filter holdings for this date
                Set<Holding> dateHoldings = new HashSet();
                Set<Holding> holdings = holdingsByDate.get(date);
                if (holdings != null) {
                    for (Holding holding : holdings) {
                        if (holding.isDeleted()) {
                            continue;
                        }
                        // check if this print holdings belongs to the manifestation
                        boolean b1 = holding.parent().equals(mid);
                        // check if manifestation is an online manifestation pointing to a print manifestation
                        boolean b2 = holding.parent().equals(pid);
                        if (b1) {
                            holding.setPrintManifestation(map.get(mid));
                            if (map.containsKey(oid)) {
                                holding.setOnlineManifestation(map.get(oid));
                                // move links from online to print manifestation
                                manifestation.setLinks(map.get(oid).getLinks());
                            }
                        } else {
                            if (b2) {
                                if (map.containsKey(pid)) {
                                    holding.setPrintManifestation(map.get(pid));
                                    holding.setOnlineManifestation(manifestation);
                                    // move links from online to print manifestation
                                    map.get(pid).setLinks(manifestation.getLinks());
                                }
                            }
                        }
                        if (b1 || b2) {
                            dateHoldings.add(holding);
                            // service for this ISIL
                            String isil = holding.getISIL();
                            if (isil != null) {
                                List<Holding> list = services.get(isil);
                                if (list == null) {
                                    list = new ArrayList();
                                    services.put(isil, list);
                                }
                                list.add(holding);
                            }
                        }
                    }
                }
                // filter licenses for this date
                Set<License> dateLicenses = new HashSet();
                Set<License> licenses = licensesByDate.get(date);
                if (licenses != null) {
                    for (License license : licenses) {
                        if (license.isDeleted()) {
                            continue;
                        }
                        boolean b1 = license.parent().equals(eid);
                        boolean b2 = license.parent().equals(oid);
                        if (b1) {
                            license.setOnlineManifestation(map.get(eid));
                            license.setPrintManifestation(map.get(pid));
                        } else if (b2) {
                            license.setOnlineManifestation(map.get(oid));
                            license.setPrintManifestation(map.get(pid));
                        }
                        if (b1 || b2) {
                            dateLicenses.add(license);
                            // service for this ISIL
                            String isil = license.getISIL();
                            if (isil != null) {
                                List<Holding> list = services.get(isil);
                                if (list == null) {
                                    list = new ArrayList();
                                    services.put(isil, list);
                                }
                                list.add(license);
                            }
                        }
                    }
                }
                if (!services.isEmpty()) {
                    foundDates.add(date);
                    writeVolume(date, manifestation, services);
                }
            }
            // not a single date with service found?
            if (foundDates.isEmpty()) {
                writeVolume(null, manifestation, null);
            } else {
                // summary
                XContentBuilder builder = jsonBuilder();
                builder.startObject()
                        .field("dates", foundDates)
                        .endObject();
                ingest.indexDocument(targetIndex,
                        targetManifestationsType,
                        manifestation.externalID(),
                        builder.string());
                writeCounter.incrementAndGet();
            }
        }
    }

    private void writeVolume(Integer date,
                             Manifestation manifestation,
                             Map<String,List<Holding>> services)
            throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject()
                .field("id", manifestation.externalID())
                .field("date", date)
                .field("title", manifestation.cleanTitle())
                .field("contentType", manifestation.contentType())
                .field("key", manifestation.getUniqueIdentifier())
                .field("identifiers", manifestation.getIdentifiers());
        if (manifestation.hasOnline()) {
            builder.field("hasOnline", manifestation.getOnlineExternalID());
        }
        if (manifestation.hasPrint()) {
            builder.field("hasPrint", manifestation.getPrintExternalID());
        }
        if (manifestation.isSupplement()) {
            builder.field("hasSupplement", manifestation.supplementExternalID());
        }
        builder.field("links", manifestation.getLinks());
        if (services != null && !services.isEmpty()) {
            builder.field("libraryCount", services.size())
                    .startArray("libraries");
            for (Map.Entry<String,List<Holding>> me : services.entrySet()) {
                builder.startObject()
                        .field("isil", me.getKey())
                        .field("serviceCount", me.getValue().size())
                        .startArray("service");
                for (Holding holding : me.getValue()) {
                    builder.startObject()
                            .field("mediaType", holding.mediaType())
                            .field("carrierType", holding.carrierType())
                            .field("id", holding.id())
                            .field("serviceisil", holding.getServiceISIL())
                            .field("info", holding.holdingInfo())
                            .endObject();
                }
                builder.endArray().endObject();
            }
            builder.endArray();
        }
        String id = manifestation.externalID() + (date != null ? "_" + date : "");
        ingest.indexDocument(targetIndex,
                targetManifestationsType,
                id,
                builder.string());
        writeCounter.incrementAndGet();
    }

    private final Map<String,String> inverseRelations = new HashMap<String,String>() {{

        put("hasPart", "isPartOf");
        put("hasSupplement", "isSupplementOf");
        put("isPartOf", "hasPart");
        put("isSupplementOf", "hasSupplement");

        // temporal axis
        put("precededBy", "succeededBy");
        put("succeededBy", "precededBy");

        // expression relations
        put("hasLanguageEdition", "isLanguageEditionOf");
        put("hasTranslation", "isTranslationOf");
        put("isLanguageEditionOf", "hasLanguageEdition");
        put("isTranslationOf", "hasTranslation");

        // manifestation (carrier) relations
        put("hasOriginalEdition", "isOriginalEditionOf");
        put("hasPrintEdition", "isPrintEditionOf");
        put("hasOnlineEdition", "isOnlineEditionOf");
        put("hasBrailleEdition", "isBrailleEditionOf");
        put("hasDVDEdition", "isDVDEditionOf");
        put("hasCDEdition", "isCDEditionOf");
        put("hasDiskEdition", "isDiskEditionOf");
        put("hasMicroformEdition", "isMicroformEditionOf");
        put("hasDigitizedEdition", "isDigitizedEditionOf");

        // manifestation (edited) relations
        put("hasSpatialEdition", "isSpatialEditionOf");
        put("hasTemporalEdition", "isTemporalEditionOf");
        put("hasPartialEdition", "isPartialEditionOf");
        put("hasTransientEdition", "isTransientEditionOf");
        put("hasLocalEdition", "isLocalEditionOf");
        put("hasAdditionalEdition", "isAdditionalEditionOf");
        put("hasAlternativeEdition", "isAdditionalEditionOf");
        put("hasDerivedEdition", "isDerivedEditionOf");
        put("hasHardcoverEdition", "isHardcoverEditionOf");
        put("hasManuscriptEdition", "isManuscriptEditionOf");
        put("hasBoxedEdition", "isBoxedEditionOf");
        put("hasReproduction", "isReproductionOf");
        put("hasSummary", "isSummaryOf");

        put("isOriginalEditionOf", "hasOriginalEdition");
        put("isPrintEditionOf", "hasPrintEdition");
        put("isOnlineEditionOf", "hasOnlineEdition");
        put("isBrailleEditionOf", "hasBrailleEdition");
        put("isDVDEditionOf", "hasDVDEdition");
        put("isCDEditionOf", "hasCDEdition");
        put("isDiskEditionOf", "hasDiskEdition");
        put("isMicroformEditionOf", "hasMicroformEdition");
        put("isDigitizedEditionOf", "hasMicroformEdition");
        put("isSpatialEditionOf", "hasSpatialEdition");
        put("isTemporalEditionOf", "hasTemporalEdition");
        put("isPartialEditionOf", "hasPartialEdition");
        put("isTransientEditionOf", "hasTransientEdition");
        put("isLocalEditionOf", "hasLocalEdition");
        put("isAdditionalEditionOf", "hasAdditionalEdition");
        put("isDerivedEditionOf", "hasDerivedEdition");
        put("isHardcoverEditionOf", "hasHardcoverEdition");
        put("isManuscriptEditionOf", "hasManuscriptEdition");
        put("isBoxedEditionOf", "hasBoxedEdition");
        put("isReproductionOf", "hasReproduction");
        put("isSummaryOf", "hasSummary");

    }};

    private class ClusterBuildContinuation {
        final Manifestation manifestation;
        final SearchResponse searchResponse;
        //final Set<String> neighbors;
        //final Set<String> visited;
        final Collection<Manifestation> cluster;
        int pos;

        ClusterBuildContinuation(Manifestation manifestation,
                                 SearchResponse searchResponse,
                                 int pos,
                                 //Set<String> neighbors,
                                 //Set<String> visited,
                                 Collection<Manifestation> cluster) {
            this.manifestation = manifestation;
            this.searchResponse = searchResponse;
            this.pos = pos;
            //this.neighbors = neighbors;
            //this.visited = visited;
            this.cluster = cluster;
        }
    }

}
