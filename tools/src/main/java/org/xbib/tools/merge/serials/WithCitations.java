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
package org.xbib.tools.merge.serials;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.elasticsearch.support.client.ingest.IngestClient;
import org.xbib.elasticsearch.support.client.search.SearchClient;
import org.xbib.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.tools.util.SearchHitPipelineElement;
import org.xbib.tools.merge.zdb.entities.Manifestation;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.FormatUtil;
import org.xbib.util.Strings;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicLong;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhrasePrefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Merge serials with citation database
 */
public class WithCitations {

    private final static Logger logger = LoggerFactory.getLogger(WithCitations.class.getName());

    private Reader reader;

    private Writer writer;

    private static Settings settings;

    // the pump
    private int numPumps;
    private BlockingQueue<SearchHitPipelineElement> pumpQueue;
    private ExecutorService pumpService;
    private CountDownLatch pumpLatch;
    private Set<MergePump> pumps;

    private Client client;
    private IngestClient ingest;

    private static AtomicLong counter = new AtomicLong(0L);

    // Elasticsearch source index/types
    private String serialIndex;
    private String serialType;

    private String sourceCitationIndex;
    private String sourceCitationType;

    // Elastiscearch target index/type
    private String targetCitationIndex;
    private String targetCitationType;

    private int size;
    private long millis;
    private String identifier;
    private Set<String> docs;

    // counters
    private long countQueries;
    private long countHits;
    private long countWrites;

    public static void main(String[] args) {
        try {
            new WithCitations()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public WithCitations reader(Reader reader) {
        this.reader = reader;
        settings = settingsBuilder().loadFromReader(reader).build();
        return this;
    }

    public WithCitations settings(Settings newSettings) {
        settings = newSettings;
        return this;
    }

    public WithCitations writer(Writer writer) {
        this.writer = writer;
        return this;
    }

    public WithCitations run() throws Exception {
        URI sourceURI = URI.create(settings.get("source"));
        SearchClient search = new SearchClient()
                .newClient(sourceURI);

        this.client = search.client();

        Map<String,String> params = URIUtil.parseQueryString(sourceURI);

        // ZDB Titel
        this.serialIndex = params.get("serialIndex");
        this.serialType = params.get("serialType");

        this.sourceCitationIndex = params.get("citationIndex");
        this.sourceCitationType = params.get("citationType");

        URI targetURI = URI.create(settings.get("target"));
        Integer maxBulkActions = settings.getAsInt("maxBulkActions", 100);
        Integer maxConcurrentBulkRequests = settings.getAsInt("maxConcurrentBulkRequests", 16);
        params = URIUtil.parseQueryString(targetURI);

        this.targetCitationIndex = params.get("citationIndex");
        if (targetCitationIndex == null) {
            targetCitationIndex = serialIndex + "citations";
        }
        this.targetCitationType = params.get("citationType");
        if (Strings.isNullOrEmpty(targetCitationType)) {
            this.targetCitationType = "citations";
        }

        this.ingest = new IngestClient()
                .maxActionsPerBulkRequest(maxBulkActions)
                .maxConcurrentBulkRequests(maxConcurrentBulkRequests)
                .newClient(targetURI);

        this.size = settings.getAsInt("getSize", 10);
        this.millis = settings.getAsLong("millis", 3600000L);
        this.identifier = settings.get("identifier");

        this.docs = Collections.synchronizedSet(new HashSet());

        this.numPumps = settings.getAsInt("numPumps", 1);;
        this.pumps = new HashSet();
        this.pumpQueue = new SynchronousQueue(true);
        this.pumpService = Executors.newFixedThreadPool(numPumps);
        this.pumpLatch = new CountDownLatch(numPumps);

        execute();

        return this;
    }

    private WithCitations execute() {
        logger.info("starting merge");

        long t0 = System.currentTimeMillis();

        for (int i = 0; i < numPumps; i++) {
            MergePump mergePump = new MergePump(i);
            pumps.add(mergePump);
            pumpService.submit(mergePump);
        }

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

        // filter only computer material?
        //FilterBuilder filterBuilder = FilterBuilders.existsFilter("physicalDescriptionElectronicResource");
        //searchRequest.setFilter(filterBuilder);

        SearchResponse searchResponse = searchRequest.execute().actionGet();

        long count = searchResponse.getHits().getTotalHits();

        counter.set(count);

        logger.info("iterating over {} serials", count);

        while (true) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                try {
                    pumpQueue.put(new SearchHitPipelineElement().set(hit));
                    counter.decrementAndGet();
                } catch (InterruptedException e) {
                    logger.error("interrupted");
                }
            }
        }
        logger.info("terminating pumps");
        for (int i = 0; i < numPumps; i++) {
            try {
                // poison element
                pumpQueue.put(new SearchHitPipelineElement().set(null));
            } catch (InterruptedException e) {
                logger.error("interrupted");
            }
        }

        logger.info("waiting for pumps");
        try {
            pumpLatch.await();
        } catch (InterruptedException e) {
            logger.error("interrupted");
        }

        logger.info("end of merge");

        for (MergePump p : pumps) {
            countQueries += p.getQueryCount();
            countHits += p.getHitCount();
            countWrites += p.getWriteCount();
        }

        long t1 = System.currentTimeMillis();
        long d = countWrites; //number of documents written
        long bytes = ingest.getTotalSizeInBytes();
        double dps = d * 1000.0 / (double)(t1 - t0);
        double avg = bytes / (d + 1.0); // avoid div by zero
        double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
        String t = TimeValue.timeValueMillis(t1 - t0).format();
        String byteSize = FormatUtil.convertFileSize(bytes);
        String avgSize = FormatUtil.convertFileSize(avg);
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("Merge with citations complete. {} docs merged, {} docs written, {} = {} ms, {} = {} bytes, {} = {} avg getSize, {} dps, {} MB/s",
                docs.size(),
                d,
                t,
                (t1-t0),
                byteSize, bytes,
                avgSize,
                formatter.format(avg),
                formatter.format(dps),
                formatter.format(mbps));

        double qps = countQueries * 1000.0 / (double)(t1 - t0);
        logger.info("queries={} qps={} hits={}",
                countQueries, formatter.format(qps),
                countHits);

        // thread pool shutdown
        pumpService.shutdownNow();

        // Elasticsearch flush and close bulk
        ingest.shutdown();

        return this;
    }

    class MergePump implements Callable<Boolean> {

        private final Logger logger;
        private final ObjectMapper mapper;

        private long countQueries;
        private long countHits;
        private long countWrite;

        public MergePump(int i) {
            this.logger = LoggerFactory.getLogger("pump" + i);
            this.mapper = new ObjectMapper();
        }

        @Override
        public Boolean call() throws Exception {
            Manifestation m = null;
            try {
                long count = 0;
                while (true) {
                    SearchHitPipelineElement t = pumpQueue.take();
                    if (t.get() == null) {
                        logger.info("received 'end of pump' message");
                        break;
                    }
                    m = new Manifestation(mapper.readValue(t.get().source(), Map.class));
                    if (filterForProcess(m)) {
                        process(m);
                    }
                    count++;
                    if (count % size == 0) {
                        // one log per cycle
                        logger.info("left={} getCount={} docs={} queries={} hits={}",
                                counter.get(), count, docs.size(), countQueries, countHits);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("pump interrupted");
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                logger.error("error while pumping {}, exiting", m, e);
                logger.error(ExceptionFormatter.format(e));
                Thread.currentThread().interrupt();
            } finally {
                pumpLatch.countDown();
            }
            return true;
        }

        public long getQueryCount() {
            return countQueries;
        }

        public long getHitCount() {
            return countHits;
        }

        public long getWriteCount() {
            return countHits;
        }

        private boolean filterForProcess(Manifestation manifestation) {
            // filter out supplement or parts
            return //"computer".equals(manifestation.mediaType()) &&
                    !manifestation.isSupplement() &&
                    !manifestation.isSubseries();
        }

        private void process(Manifestation manifestation) throws IOException {
            String docid = manifestation.id();
            if (docs.contains(docid)) {
                return;
            }
            docs.add(docid);

            // Build query.
            // ISSN (obligatory if any),
            // title (obligatory if no ISSN),
            // fromDate/toDate (range if title),
            // publisher (if title and not issn)

            boolean hasISSN = false;
            boolean hasTitle = false;

            BoolQueryBuilder queryBuilder = boolQuery();

            // extract given ISSNs
            List<String> issns = new LinkedList();
            Map<String,Object> m = manifestation.getIdentifiers();
            if (m != null) {
                for (String k : m.keySet()) {
                    if ("issn".equals(k)) {
                        Object o = m.get(k);
                        if (o == null) {
                            continue;
                        }
                        List<Map<String,Object>> list = null;
                        if (o instanceof Map) {
                            list = Arrays.asList((Map<String, Object>) m.get(k));
                        }
                        else if (o instanceof List) {
                            list = (List<Map<String,Object>>)m.get(k);
                        } else {
                            issns.add(o.toString());
                        }
                        if (list != null) {
                            for (Map<String, Object> mm : list) {
                                issns.add( (String)mm.get("value") );
                            }
                        }
                    }
                }
                if (!issns.isEmpty()) {
                    BoolQueryBuilder issnQuery = boolQuery();
                    for (String issn : issns) {
                        if (issn != null) {
                            issnQuery.should(matchPhraseQuery("prism:issn", issn));
                        }
                    }
                    queryBuilder.must(issnQuery);
                    hasISSN = true;
                }
            }

            if (!hasISSN) {
                // shorten title (series statement after '/' or ':') to raise probability of matching.
                String t = manifestation.title();
                int pos = t.indexOf('/');
                if (pos > 0) {
                    t = t.substring(0,pos - 1);
                }
                pos = t.indexOf(':');
                if (pos > 0) {
                    t = t.substring(0,pos - 1);
                }
                QueryBuilder titleQuery = matchPhrasePrefixQuery("prism:publicationName", t);
                hasTitle = true;
                QueryBuilder dateQuery = rangeQuery("prism:publicationDate")
                        .gte(manifestation.firstDate())
                        .lte(manifestation.lastDate());
                queryBuilder.must(titleQuery).must(dateQuery);
            }

            if (!hasISSN && hasTitle) {
                String p = manifestation.getString("PublicationStatement.publisherName");
                QueryBuilder publisherQuery = p != null ? matchPhraseQuery("dc:publisher", p) : null;
                if (publisherQuery != null) {
                    queryBuilder.must(publisherQuery);
                }
            }

            if (!hasISSN && !hasTitle) {
                logger.error("no ISSN and no title in {}", manifestation);
                return;
            }

            SearchRequestBuilder searchRequest = client.prepareSearch()
                    .setIndices(sourceCitationIndex)
                    .setTypes(sourceCitationType)
                    .setQuery(queryBuilder)
                    .setSize(size) // getSize is per shard!
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(millis));
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            countQueries++;
            SearchHits hits = searchResponse.getHits();
            countHits += hits.getTotalHits();
            if (hits.getHits().length == 0) {
                return;
            }
            logger.info("hits = {} (total {}) q={}",
                 hits.getTotalHits(), countHits, queryBuilder.toString().replaceAll("\\s","").replaceAll("\\n", ""));
            do {
                hits = searchResponse.getHits();
                for (int i = 0; i < hits.getHits().length; i++ ) {
                    SearchHit hit = hits.getAt(i);
                    copyToTarget(hit, manifestation);
                }
                searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(millis))
                        .execute().actionGet();
                countQueries++;
                hits = searchResponse.getHits();
            } while (hits.getHits().length > 0);
        }

        private void copyToTarget(SearchHit hit, Manifestation manifestation) throws IOException {
            XContentBuilder builder = jsonBuilder();
            Map<String,Object> m = hit.sourceAsMap();
            Map<String,Object> publication = (Map<String,Object>)m.get("frbr:partOf");
            if (publication != null) {
                String id = manifestation.externalID();
                publication.put("xbib:zdbid", id);
                // unique endeavor key, independent of ISSN
                //publication.put("dcterms:identifier", manifestation.getUniqueIdentifier());
                // hyphenated form od ZDB ID
                String zdborig = new StringBuilder(id).insert(id.length() - 1, "-").toString();
                // link to ZDB service
                IRI zdbserviceid = IRI.builder()
                        .scheme("http")
                        .host("ld.zdb-services.de")
                        .path("/getResource/" + zdborig).build();
                publication.put("dcterms:identifier", zdbserviceid.toString());
            }
            m.put("frbr:partOf", publication);
            builder.value(m);
            String doiPart = hit.id().replaceAll("%2F","/");
            IRI id = IRI.builder()
                    .scheme("http")
                    .host("xbib.info")
                    .path("/endeavors/doi").fragment(doiPart).build();
            ingest.index(targetCitationIndex,
                    targetCitationType,
                    id.toString(),
                    builder.string());
        }

    }
}
