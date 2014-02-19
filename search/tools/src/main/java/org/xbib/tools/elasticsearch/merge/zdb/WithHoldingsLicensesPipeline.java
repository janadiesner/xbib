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
package org.xbib.tools.elasticsearch.merge.zdb;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import org.xbib.common.settings.Settings;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.tools.elasticsearch.merge.SearchHitPipelineElement;
import org.xbib.tools.elasticsearch.merge.zdb.entities.TimeLine;
import org.xbib.tools.elasticsearch.merge.zdb.entities.Cluster;
import org.xbib.tools.elasticsearch.merge.zdb.entities.Holding;
import org.xbib.tools.elasticsearch.merge.zdb.entities.Indicator;
import org.xbib.tools.elasticsearch.merge.zdb.entities.License;
import org.xbib.tools.elasticsearch.merge.zdb.entities.Manifestation;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.metrics.MeterMetric;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequestListener;
import org.xbib.util.ExceptionFormatter;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class WithHoldingsLicensesPipeline implements Pipeline<Boolean, Manifestation> {

    private final static Object lock = new Object();

    private final int number;

    private final WithHoldingsLicenses service;

    private final Logger logger;

    private final ObjectMapper mapper;

    private final Queue<ClusterBuildContinuation> buildQueue;

    private Map<String, PipelineRequestListener> listeners;

    private Set<Manifestation> candidates;

    private SearchHitPipelineElement t;

    private Manifestation m;

    private String sourceTitleIndex;
    private String sourceTitleType;

    private String sourceHoldingsIndex;
    private String sourceHoldingsType;
    private String sourceLicenseIndex;
    private String sourceLicenseType;
    private String sourceIndicatorIndex;
    private String sourceIndicatorType;

    private final String index;
    private final String worksIndexType;
    private final String manifestationsIndexType;
    private final String volumesIndexType;
    private final String holdingsIndexType;

    private MeterMetric metric;

    public WithHoldingsLicensesPipeline(WithHoldingsLicenses service, int number) {
        this.number = number;
        this.service = service;
        this.listeners = newHashMap();
        this.buildQueue = new ConcurrentLinkedQueue();
        this.logger = LoggerFactory.getLogger(toString());
        this.mapper = new ObjectMapper();
        Settings settings = service.settings();
        this.sourceTitleIndex = settings.get("bibIndex");
        this.sourceTitleType = settings.get("bibType");
        this.sourceHoldingsIndex = settings.get("holIndex");
        this.sourceHoldingsType = settings.get("holType");
        this.sourceLicenseIndex = settings.get("licenseIndex");
        this.sourceLicenseType = settings.get("licenseType");
        this.sourceIndicatorIndex = settings.get("indicatorIndex");
        this.sourceIndicatorType = settings.get("indicatorType");

        this.index = settings.get("index");
        if (index == null) {
            throw new IllegalArgumentException("no index given");
        }
        this.worksIndexType = "xbib:Work";
        this.manifestationsIndexType = "xbib:Manifestation";
        this.volumesIndexType = "xbib:Volume";
        this.holdingsIndexType = "xbib:Holding";
    }

    public Queue<ClusterBuildContinuation> getBuildQueue() {
        return buildQueue;
    }

    public Collection<Manifestation> getCluster() {
        return candidates;
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
            logger.error("exception while processing {}, exiting", m);
            logger.error(ExceptionFormatter.format(e));
        } finally {
            service.countDown();
            metric.stop();
        }
        logger.info("pipeline terminating");
        return true;
    }

    @Override
    public MeterMetric getMetric() {
        return metric;
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
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        if (!service.queue().isEmpty()) {
            logger.error("service queue not empty?");
        }
        if (!buildQueue.isEmpty()) {
            logger.error("build queue not empty?");
        }
        logger.info("closing");
    }

    public Pipeline<Boolean, Manifestation> add(String name,PipelineRequestListener listener) {
        this.listeners.put(name, listener);
        return this;
    }

    @Override
    public String toString() {
        return WithHoldingsLicenses.class.getSimpleName() + "." + number;
    }

    private void process(Manifestation manifestation) throws IOException {
        String id = manifestation.externalID();
        // Skip supplements and manifestations in timeline here.
        // All of them are referenced from another manifestation.
        // We pull them in while cluster construction.
        // We can not skip carrier types here except hasPrintEdition.
        if (manifestation.isInTimeline() || manifestation.isSupplement() || manifestation.hasPrint()) {
            service.skipped().add(id);
            return;
        }
        // if we had this seen, it is already in the global state, so skip it.
        if (service.docs().contains(id)) {
            return;
        }

        // book keeping for our local docs. We add them to the global docs at last.
        Set<String> docs = newHashSet();
        docs.add(id);

        // create new candidate set.
        // Candidates are unstructured, no timeline organization,
        // no relationship analysis, ordered by ID
        this.candidates = newTreeSet(Manifestation.getComparator());
        candidates.add(manifestation);
        // retrieve all docs that are connected by relationships into the candidate set.
        retrieveCluster(candidates, manifestation);

        // while we process, there may be other thread stealing our clusters, children etc.
        // So we work with immutable copies.

        // microforms et al?
        //Set<Manifestation> c1 = ImmutableSet.copyOf(candidates);
        //candidates.addAll(extractOtherEditions(c1));

        // Ensure direct relationships in the cluster.
        Set<Manifestation> c2 = ImmutableSet.copyOf(candidates);
        for (Manifestation m : c2) {
            setAllRelationsBetween(m, c2);
        }

        // Create timelines, and process each timeline for services (holdings, licenses, indicators)
        Cluster c = new Cluster(ImmutableSet.copyOf(candidates));
        List<TimeLine> timeLines = c.timeLines();

        for (TimeLine timeLine : timeLines) {

            String timelineId = timeLine.first().externalID();

            // precaution against processing a timeline again. This should not happen.
            if (service.timelines().contains(timelineId)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("timeline {} already exists (while processing manifestation {})",
                            timelineId, manifestation);
                }
                continue;
            }
            service.timelines().add(timelineId);

            // Now, this is expensive. Find holdings, licenses, indicators of this time line
            timeLine.setHoldings(searchHoldings(timeLine));
            timeLine.setLicenses(searchLicenses(timeLine));
            timeLine.setIndicators(searchIndicators(timeLine));

            // index a summary of the time line
            XContentBuilder builder = jsonBuilder();
            timeLine.build(builder);
            service.ingest().index(index, worksIndexType, timelineId, builder.string());
            builder.close();

            // stratify and re-adjust the services in the timeline.
            timeLine.makeServices();

            /*if (logger.isDebugEnabled()) {
            Map<Integer,Map<String,List<Holding>>> servicesByDate = timeLine.getServicesByDate();
                logger.debug("servicesByDate = {}", servicesByDate);
            }*/

            // index every manifestation in the timeline
            Set<String> visited = newHashSet();
            for (Manifestation m : timeLine) {
                indexManifestation(m, visited);
            }
            docs.addAll(visited);

            if (logger.isDebugEnabled()) {
                logger.debug("timeline {}/{}/{} of size {}: indexed {} manifestations",
                        index, worksIndexType, timelineId,
                    timeLine.size(), visited.size()
                );
            }
        }

        // not in timeline?
        Set<String> visited = newHashSet();
        for (Manifestation m : c.notInTimeLines()) {

            indexManifestation(m, visited);
        }
        docs.addAll(visited);

        // At last, put all timeline doc ids to the global state
        synchronized (lock) {
            service.docs().addAll(docs);
        }
    }

    private void indexManifestation(Manifestation m, Set<String> visited) throws IOException {
        String id = m.externalID();
        // protection against recursion (should not happen)
        if (visited.contains(id)) {
            return;
        }
        visited.add(id);
        // make sure at global level we do never index a manifestation twice
        if (service.indexed().contains(id)) {
            return;
        }
        service.indexed().add(id);
        // first, index related manifestations
        ImmutableMultimap<String,Manifestation> related = ImmutableMultimap.copyOf(m.getRelatedManifestations());
        for (Manifestation mm : related.values()) {
            indexManifestation(mm, visited);
        }
        // second, index this manifestation
        XContentBuilder builder = jsonBuilder();
        m.build(builder, null);
        String s = builder.string();
        if (logger.isTraceEnabled()) {
            logger.trace("indexing {}", s);
        }
        service.ingest().index(index, manifestationsIndexType, id, s);
        builder.close();

        // get all the evidences, and index them twice: by date and by holder

        SetMultimap<Integer,Holding> evidenceByDate = m.getEvidenceByDate();
        // index volumes
        for (Integer date : evidenceByDate.keySet()) {
            XContentBuilder datebuilder = jsonBuilder();
            m.buildVolume(datebuilder, m.externalID(), date, evidenceByDate.get(date));
            id = m.externalID() + "_" + (date != null ? date : "");
            s = datebuilder.string();
            service.ingest().index(index, volumesIndexType, id, s);
            builder.close();
        }
        // index by holders
        SetMultimap<String,Holding> holdings = m.getEvidenceByHolder();
        for (String holder : holdings.keySet()) {
            XContentBuilder holdingsBuilder = jsonBuilder();
            m.buildHolding(holdingsBuilder, m.externalID(), holder, holdings.get(holder));
            id = m.externalID() + "_" + holder;
            s = holdingsBuilder.string();
            service.ingest().index(index, holdingsIndexType, id, s);
            builder.close();
        }
    }

    private void retrieveCluster(Collection<Manifestation> cluster,
                                 Manifestation manifestation) throws IOException {
        SetMultimap<String, String> relations = manifestation.getRelations();
        Set<String> neighbors = newHashSet(relations.values());
        QueryBuilder queryBuilder = neighbors.isEmpty() ?
                termQuery("_all",  manifestation.id()) :
                boolQuery().should(termQuery("_all",  manifestation.id()))
                        .should(termsQuery("IdentifierDNB.identifierDNB", neighbors.toArray()));
        SearchRequestBuilder searchRequest = service.client().prepareSearch()
                .setQuery(queryBuilder)
                .setSize(service.size()) // getSize is per shard!
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(service.millis()));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMillis(service.millis()))
                .execute().actionGet();
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
                Manifestation m = new Manifestation(mapper.readValue(hit.source(), Map.class));
                boolean collided = detectCollisionAndTransfer(m, c, i);
                if (collided) {
                    return;
                }
                // check for local cluster docs (avoid loops)
                if (c.cluster.contains(m)) {
                    continue;
                }
                c.cluster.add(m);
                boolean temporalRelation = false;
                boolean carrierRelation = false;
                for (String relation : findTheRelationsBetween(c.manifestation, m.id())) {
                    if (relation == null) {
                        logger.debug("unknown relation {}", relation);
                        continue;
                    }
                    c.manifestation.addRelatedManifestation(relation, m);
                    String inverse = inverseRelations.get(relation);
                    if (inverse == null) {
                        logger.debug("no inverse relation for {}", relation);
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
                        logger.debug("unknown relation {}", relation);
                        continue;
                    }
                    m.addRelatedManifestation(relation, c.manifestation);
                    String inverse = inverseRelations.get(relation);
                    if (inverse == null) {
                        logger.debug("no inverse relation for {}", relation);
                    } else {
                        c.manifestation.addRelatedManifestation(inverse, m);
                    }
                    temporalRelation = temporalRelation
                            || "precededBy".equals(relation)
                            || "succeededBy".equals(relation);
                    carrierRelation = carrierRelation
                            || Manifestation.carrierEditions().contains(relation);
                }
                // Expand cluster for this manifestation iff temporal or carrier relation.
                // Also expand if there are any other print/online editions.
                if (temporalRelation || carrierRelation || m.hasCarrierRelations()) {
                    retrieveCluster(c.cluster, m);
                }
            }
            searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .execute().actionGet();
            hits = searchResponse.getHits();
        } while (hits.getHits().length > 0);
    }

    private boolean detectCollisionAndTransfer(Manifestation manifestation,
                                               ClusterBuildContinuation c, int pos) {
        for (WithHoldingsLicensesPipeline pipeline : service.getPipelines()) {
            if (this == pipeline) {
                continue;
            }
            if (pipeline.getCluster() != null && pipeline.getCluster().contains(manifestation)) {
                logger.debug("collision detected for {} at hit pos {}, with {} ",
                        manifestation, pos, pipeline);
                c.pos = pos;
                pipeline.getBuildQueue().offer(c);
                return true;
            }
        }
        return false;
    }

    private Set<Holding> searchHoldings(Set<Manifestation> manifestations) throws IOException {
        Map<String,Manifestation> map = newHashMap();
        for (Manifestation m : manifestations) {
            map.put(m.id(), m);
            if (m.getPrintID() != null) {
                map.put(m.getPrintID(), m);
            }
        }
        return searchHoldings(map);
    }

    private Set<Holding> searchHoldings(Map<String,Manifestation> manifestations) throws IOException {
        Set<Holding> holdings = newHashSet();
        if (sourceHoldingsIndex == null) {
            return holdings;
        }
        if (manifestations == null || manifestations.isEmpty()) {
            return holdings;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = manifestations.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            // we search in _all  (better would be parent)
            QueryBuilder queryBuilder = termsQuery("identifierParent", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = service.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(service.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(service.millis()));
            searchRequest.setIndices(sourceHoldingsIndex);
            if (sourceHoldingsType != null) {
                searchRequest.setTypes(sourceHoldingsType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            while (searchResponse.getScrollId() != null) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    Holding holding = new Holding(mapper.readValue(hit.source(), Map.class));
                    if (holding.isDeleted()) {
                        continue;
                    }
                    String isil = holding.getISIL();
                    if (isil == null || !isil.startsWith("DE-")) {
                        continue;
                    }
                    String organization = service.bibdatLookup().lookup().get(isil);
                    holding.setOrganization(organization);
                    Manifestation parent = manifestations.get(holding.parent());
                    if (parent == null) {
                        logger.debug("no parent for {}: {}", holding.parent(), holding);
                        continue;
                    }
                    parent.addRelatedHolding(isil, holding);
                    holding.setManifestation(parent);
                    holdings.add(holding);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("found {} holdings for manifestations {}",
                    holdings.size(), manifestations);
        }
        return holdings;
    }

    private Set<License> searchLicenses(Set<Manifestation> manifestations) throws IOException {
        Map<String,Manifestation> map = newHashMap();
        boolean hasOnline = false;
        for (Manifestation m : manifestations) {
            map.put(m.externalID(), m);
            hasOnline = hasOnline || m.hasOnline();
            if (m.getOnlineExternalID() != null) {
                map.put(m.getOnlineExternalID(), m);
            }
        }
        return hasOnline ? searchLicenses(map) : null;
    }

    private Set<License> searchLicenses(Map<String,Manifestation> manifestations) throws IOException {
        Set<License> licenses = newHashSet();
        if (sourceLicenseIndex == null) {
            return licenses;
        }
        if (manifestations == null || manifestations.isEmpty()) {
            return licenses;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = manifestations.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            QueryBuilder queryBuilder = termsQuery("ezb:zdbid", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = service.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(service.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(service.millis()));
            searchRequest.setIndices(sourceLicenseIndex);
            if (sourceLicenseType != null) {
                searchRequest.setTypes(sourceLicenseType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            while (true) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    License license = new License(mapper.readValue(hit.source(), Map.class));
                    if (license.isDeleted()) {
                        continue;
                    }
                    String isil = license.getISIL();
                    if (isil == null || !isil.startsWith("DE-")) {
                        continue;
                    }
                    String organization = service.bibdatLookup().lookup().get(isil);
                    license.setOrganization(organization);
                    Manifestation m = manifestations.get(license.parent());
                    if (m == null) {
                        continue;
                    }
                    m.addRelatedHolding(isil, license);
                    license.setManifestation(m);
                    licenses.add(license);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("found {} licenses for manifestations {}",
                    licenses.size(), manifestations);
        }
        return licenses;
    }

    private Set<Indicator> searchIndicators(Set<Manifestation> manifestations) throws IOException {
        Map<String,Manifestation> map = newHashMap();
        boolean hasOnline = false;
        for (Manifestation m : manifestations) {
            map.put(m.externalID(), m);
            hasOnline = hasOnline || m.hasOnline();
            if (m.getOnlineExternalID() != null) {
                map.put(m.getOnlineExternalID(), m);
            }
        }
        return hasOnline ? searchIndicators(map) : null;
    }

    private Set<Indicator> searchIndicators(Map<String,Manifestation> manifestations) throws IOException {
        Set<Indicator> indicators = newHashSet();
        if (sourceIndicatorIndex == null) {
            return indicators;
        }
        if (manifestations == null || manifestations.isEmpty()) {
            return indicators;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = manifestations.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            QueryBuilder queryBuilder = termsQuery("xbib:identifier", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = service.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(service.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(service.millis()));
            searchRequest.setIndices(sourceIndicatorIndex);
            if (sourceLicenseType != null) {
                searchRequest.setTypes(sourceIndicatorType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            while (true) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    Indicator indicator = new Indicator(mapper.readValue(hit.source(), Map.class));
                    if (!indicator.getISIL().startsWith("DE-")) {
                        continue;
                    }
                    String organization = service.bibdatLookup().lookup().get(indicator.getISIL());
                    indicator.setOrganization(organization);
                    if (logger.isTraceEnabled()) {
                        logger.trace("resolved organization for {}: {}", indicator.getISIL(), organization);
                    }
                    Manifestation m = manifestations.get(indicator.parent());
                    if (m == null) {
                        continue;
                    }
                    m.addRelatedHolding(indicator.getISIL(), indicator);
                    indicator.setManifestation(m);
                    indicators.add(indicator);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("found {} indicators for manifestations {}",
                    indicators.size(), manifestations);
        }
        return indicators;
    }

    private Set<String> findTheRelationsBetween(Manifestation manifestation, String id) {
        if (manifestation.id().equals(id)) {
            return null;
        }
        Set<String> relationNames = new HashSet<String>();
        for (String entry : Manifestation.relationEntries()) {
            Object o = manifestation.map().get(entry);
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
                }
                for (Object obj : (List) o) {
                    Map<String, Object> m = (Map<String, Object>) obj;
                    String value = (String)m.get("identifierDNB");
                    if (id.equals(value)) {
                        Object oo = m.get("relation");
                        if (oo != null) {
                            if (!(oo instanceof List)) {
                                oo = Arrays.asList(oo);
                            }
                            for (Object relName : (List)oo) {
                                relationNames.add(relName.toString());
                            }
                        }
                    }
                }
            }
        }
        return relationNames;
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
                        if (logger.isTraceEnabled()) {
                            logger.trace("entry {} has no relation name in {}", entry, manifestation.externalID());
                        }
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
                            if (inverse != null) {
                                m.addRelatedManifestation(inverse, manifestation);
                            } else {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("no inverse relation for {} in {}", key, manifestation.externalID());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Collection<Manifestation> extractOtherEditions(Collection<Manifestation> cluster) {
        Collection<Manifestation> children = newLinkedList();
        /*for (Manifestation manifestation : cluster) {
            Manifestation microform = manifestation.cloneMicroformEdition();
            if (microform != null) {
                children.add(microform);
            }
        }*/
        return children;
    }


    private class ClusterBuildContinuation {
        final Manifestation manifestation;
        final SearchResponse searchResponse;
        final Collection<Manifestation> cluster;
        int pos;

        ClusterBuildContinuation(Manifestation manifestation,
                                 SearchResponse searchResponse,
                                 int pos,
                                 Collection<Manifestation> cluster) {
            this.manifestation = manifestation;
            this.searchResponse = searchResponse;
            this.pos = pos;
            this.cluster = cluster;
        }
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
}
