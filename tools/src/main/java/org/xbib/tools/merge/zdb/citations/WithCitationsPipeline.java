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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.iri.IRI;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequestListener;
import org.xbib.tools.merge.zdb.entities.Manifestation;
import org.xbib.tools.util.SearchHitPipelineElement;
import org.xbib.util.ExceptionFormatter;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhrasePrefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

public class WithCitationsPipeline implements Pipeline<Boolean, Manifestation> {

    private final WithCitations service;

    private final int number;

    private final Logger logger;

    private final ObjectMapper mapper;

    private SearchHitPipelineElement t;

    private Manifestation m;

    private MeterMetric metric;

    private Map<String, PipelineRequestListener> listeners;

    public WithCitationsPipeline(WithCitations service, int number) {
        this.number = number;
        this.service = service;
        this.logger = LogManager.getLogger(toString());
        this.mapper = new ObjectMapper();
        this.listeners = newHashMap();
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public Integer getNumber() {
        return null;
    }

    public Pipeline<Boolean, Manifestation> add(String name, PipelineRequestListener listener) {
        this.listeners.put(name, listener);
        return this;
    }

    @Override
    public MeterMetric getMetric() {
        return metric;
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("pipeline starting");
        try {
            this.metric = new MeterMetric(5L, TimeUnit.SECONDS);
            while (hasNext()) {
                m = next();
                process(m);
                for (PipelineRequestListener listener : listeners.values()) {
                    listener.newRequest(this, m);
                }
                metric.mark();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            logger.error(ExceptionFormatter.format(e));
            logger.error("exiting, exception while processing {}", m);
        } finally {
            service.countDown();
            metric.stop();
        }
        logger.info("pipeline terminating");
        return true;
    }

    @Override
    public boolean hasNext() {
        try {
            t = service.queue().poll(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            t = null;
            logger.error("pipeline processing interrupted, queue inactive", e);
        }
        return t != null && t.get() != null;
    }

    @Override
    public Manifestation next() {
        try {
            return new Manifestation(mapper.readValue(t.get().source(), Map.class));
        } catch (IOException e) {
            logger.error("exception while processing, exiting", e);
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        if (!service.queue().isEmpty()) {
            logger.error("service queue not empty?");
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return WithCitationsPipeline.class.getSimpleName() + "." + number;
    }

    private void process(Manifestation manifestation) throws IOException {
        String docid = manifestation.id();
        if (service.docs().contains(docid)) {
            return;
        }
        service.docs().add(docid);

        // Build query.
        // ISSN (obligatory if any),
        // title (obligatory if no ISSN),
        // fromDate/toDate (range if title),
        // publisher (if title and not issn)

        BoolQueryBuilder queryBuilder = boolQuery();

        // extract given ISSNs
        List<String> issns = new LinkedList();
        Map<String, Object> m = manifestation.getIdentifiers();
        if (m != null) {
            for (String k : m.keySet()) {
                if ("issn".equals(k)) {
                    // "IdentifierISSN":[{"value":"0006-3002"},{"value":"0005-2728"},{"value":"0005-2736"},{"value":"0304-4165"},{"value":"0167-4838"},{"value":"1388-1981"},{"value":"0167-4889"},{"value":"0167-4781"},{"value":"0304-419X"},{"value":"1570-9639"},{"value":"0925-4439"},{"value":"1874-9399"}]
                    Object o = m.get(k);
                    if (o == null) {
                        continue;
                    }
                    List list = null;
                    if (o instanceof Map) {
                        list = Arrays.asList(m.get(k));
                    } else if (o instanceof List) {
                        list = (List) m.get(k);
                    } else {
                        issns.add(o.toString());
                    }
                    if (list != null) {
                        for (Object oo : list) {
                            if (oo == null) {
                                continue;
                            }
                            List list2 = null;
                            if (oo instanceof Map) {
                                list2 = Arrays.asList(((Map) oo).get("value"));
                            } else if (oo instanceof List) {
                                list2 = (List) oo;
                            } else {
                                issns.add(oo.toString());
                            }
                            if (list2 != null) {
                                for (Object ooo : list2) {
                                    if (ooo == null) {
                                        continue;
                                    }
                                    issns.add(ooo.toString());
                                }
                            }
                        }
                    }
                }
            }
            if (!issns.isEmpty()) {
                BoolQueryBuilder issnQuery = boolQuery();
                for (String issn : issns) {
                    if (issn != null) {
                        String s = issn.indexOf('-') > 0 ? issn :
                                new StringBuilder(issn.toLowerCase()).insert(4, '-').toString();
                        issnQuery.should(matchPhraseQuery("prism:issn", s));
                    }
                }
                queryBuilder.must(issnQuery);
            }
        }
        if (issns.isEmpty()) {
            // title matching, shorten title (series statement after '/' or ':') to raise probability of matching
            String t = manifestation.title();
            int pos = t.indexOf('/');
            if (pos > 0) {
                t = t.substring(0, pos - 1);
            }
            pos = t.indexOf(':');
            if (pos > 0) {
                t = t.substring(0, pos - 1);
            }
            QueryBuilder titleQuery = matchPhrasePrefixQuery("prism:publicationName", t);
            QueryBuilder dateQuery = rangeQuery("prism:publicationDate")
                    .gte(manifestation.firstDate())
                    .lte(manifestation.lastDate());
            queryBuilder.must(titleQuery).must(dateQuery);
            // filter to publisher, if exists
            String p = manifestation.publisher();
            QueryBuilder publisherQuery = p != null ? matchPhraseQuery("dc:publisher", p) : null;
            if (publisherQuery != null) {
                queryBuilder.must(publisherQuery);
            }
        }
        SearchRequestBuilder searchRequest = service.client().prepareSearch()
                .setIndices(service.settings().get("sourceCitationIndex"))
                .setTypes(service.settings().get("sourceCitationType"))
                .setQuery(queryBuilder)
                .setSize(service.size()) // getSize is per shard!
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(service.millis()));
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMillis(service.millis()))
                .execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        if (logger.isTraceEnabled()) {
            logger.trace("hits={} q={}", hits.getTotalHits(), queryBuilder.toString().replaceAll("\\n", ""));
        }
        if (hits.getHits().length == 0) {
            return;
        }
        do {
            hits = searchResponse.getHits();
            for (int i = 0; i < hits.getHits().length; i++) {
                SearchHit hit = hits.getAt(i);
                copyToTarget(hit, manifestation);
            }
            searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .execute().actionGet();
            hits = searchResponse.getHits();
        } while (hits.getHits().length > 0);
    }

    private void copyToTarget(SearchHit hit, Manifestation manifestation) throws IOException {
        XContentBuilder builder = jsonBuilder();
        Map<String, Object> m = hit.sourceAsMap();
        Map<String, Object> publication = (Map<String, Object>) m.get("frbr:partOf");
        if (publication != null) {
            // check for 'CrossRef' tags in dc:publisher
            String publisher = (String) publication.get("dc:publisher");
            if (publisher != null && publisher.contains("CrossRef")) {
                publication.remove("dc:publisher");
            }
            String id = manifestation.externalID();
            // unique endeavor key, independent of ISSN
            //publication.put("dcterms:identifier", manifestation.getUniqueIdentifier());
            // un-hyphenated from of ZDB ID
            publication.put("xbib:zdbid", id);
            // hyphenated form of ZDB ID goes to ld.zdb-services.de
            String zdborig = new StringBuilder(id).insert(id.length() - 1, "-").toString();
            // link to ZDB service
            IRI zdbserviceid = IRI.builder()
                    .scheme("http")
                    .host("ld.zdb-services.de")
                    .path("/resource/" + zdborig).build();
            publication.put("dcterms:identifier", zdbserviceid.toString());
        }
        m.put("frbr:partOf", publication);
        if (m.containsKey("prism:publicationDate")) {
            // add firstdate, lastdate (for sort)
            Object o = m.get("prism:publicationDate");
            m.put("firstdate", o);
            m.put("lastdate", o);
        }

        builder.value(m);

        String doiPart = hit.id().replaceAll("%2F", "/");
        String type = service.settings().get("targetCitationType");
        // if author, move to citation with authors
        if (m.containsKey("dc:creator")) {
            type = type + "WithCreator";
        }
        IRI id = IRI.builder()
                .scheme("http")
                .host("xbib.info")
                .path("/endeavors/doi").fragment(doiPart).build();
        service.ingest().index(service.settings().get("targetCitationIndex"),
                type,
                id.toString(),
                builder.string());
    }
}
