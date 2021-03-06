package org.xbib.tools.marc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.common.hppc.cursors.ObjectCursor;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.xbib.common.unit.ByteSizeValue;
import org.xbib.entities.marc.MARCEntityBuilderState;
import org.xbib.entities.marc.MARCEntityQueue;
import org.xbib.entities.support.ClasspathURLStreamHandler;
import org.xbib.entities.support.ValueMaps;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.Feeder;
import org.xbib.tools.TimewindowFeeder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for MARC bibliographic data
 */
public abstract class BibliographicFeeder extends TimewindowFeeder {

    private final static Logger logger = LogManager.getLogger(BibliographicFeeder.class.getSimpleName());

    private static String concreteIndex;

    @Override
    protected String getIndex() {
        return settings.get("bib-index");
    }

    protected String getConcreteIndex() {
        return concreteIndex;
    }

    @Override
    protected String getType() {
        return settings.get("bib-type");
    }

    @Override
    protected BibliographicFeeder prepare() throws IOException {
        ingest = createIngest();
        Integer maxbulkactions = settings.getAsInt("maxbulkactions", 1000);
        Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                Runtime.getRuntime().availableProcessors());
        ingest.maxActionsPerBulkRequest(maxbulkactions)
                .maxConcurrentBulkRequests(maxconcurrentbulkrequests);
        ingest.newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .build());
        String timeWindow = settings.get("timewindow") != null ?
                DateTimeFormat.forPattern(settings.get("timewindow")).print(new DateTime()) : "";
        concreteIndex = resolveAlias(getIndex() + timeWindow);
        logger.info("base index name = {}, concrete index name = {}",
                getIndex(), getConcreteIndex());
        super.prepare();
        return this;
    }

    @Override
    protected BibliographicFeeder createIndex(String index) throws IOException {
        if (ingest.client() == null) {
            return this;
        }
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        if (settings.getAsBoolean("onlyalias", false)) {
            updateAliases();
            return this;
        }
        try {
            String indexSettings = settings.get("bib-index-settings",
                    "classpath:org/xbib/tools/feed/elasticsearch/marc/bib-settings.json");
            InputStream indexSettingsInput = (indexSettings.startsWith("classpath:") ?
                    new URL(null, indexSettings, new ClasspathURLStreamHandler()) :
                    new URL(indexSettings)).openStream();
            String indexMappings = settings.get("bib-index-mapping",
                    "classpath:org/xbib/tools/feed/elasticsearch/marc/bib-mapping.json");
            InputStream indexMappingsInput = (indexMappings.startsWith("classpath:") ?
                    new URL(null, indexMappings, new ClasspathURLStreamHandler()) :
                    new URL(indexMappings)).openStream();
            ingest.newIndex(getConcreteIndex(), getType(), indexSettingsInput, indexMappingsInput);
            indexSettingsInput.close();
            indexMappingsInput.close();
        } catch (Exception e) {
            if (!settings.getAsBoolean("ignoreindexcreationerror", false)) {
                throw e;
            } else {
                logger.warn("index creation error, but configured to ignore", e);
            }
        }
        ingest.startBulk(getConcreteIndex(), -1, 1000);
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        if (settings.getAsBoolean("onlyalias", false)) {
            return;
        }
        // set identifier prefix (ISIL)
        Map<String,Object> params = newHashMap();
        params.put("identifier", settings.get("identifier", "DE-605"));
        params.put("_prefix", "(" + settings.get("identifier", "DE-605") + ")");
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        final MARCEntityQueue queue = createQueue(params);
        queue.setUnmappedKeyListener((id,key) -> {
                    if ((settings.getAsBoolean("detect-unknown", false))) {
                        logger.warn("record {} unmapped field {}", id, key);
                        unmapped.add("\"" + key + "\"");
                    }
                });
        queue.execute();
        String fileName = uri.getSchemeSpecificPart();
        InputStream in = new FileInputStream(fileName);
        ByteSizeValue bufferSize = settings.getAsBytesSize("buffersize", ByteSizeValue.parseBytesSizeValue("1m"));
        if (fileName.endsWith(".gz")) {
            in = bufferSize != null ? new GZIPInputStream(in, bufferSize.bytesAsInt()) : new GZIPInputStream(in);
        }
        process(in, queue);
        queue.close();
        if (settings.getAsBoolean("detect-unknown", false)) {
            logger.info("unknown keys={}", unmapped);
        }
    }

    @Override
    protected BibliographicFeeder cleanup() throws IOException {
        if (settings.getAsBoolean("aliases", false) && !settings.getAsBoolean("mock", false) && ingest.client() != null) {
            updateAliases();
        } else {
            logger.info("not doing alias settings");
        }
        ingest.stopBulk(getConcreteIndex());
        super.cleanup();
        return this;
    }

    protected String resolveAlias(String alias) {
        if (ingest.client() == null) {
            return alias;
        }
        GetAliasesResponse getAliasesResponse = ingest.client().admin().indices().prepareGetAliases(alias).execute().actionGet();
        if (!getAliasesResponse.getAliases().isEmpty()) {
            return getAliasesResponse.getAliases().keys().iterator().next().value;
        }
        return alias;
    }

    protected void updateAliases() {
        super.updateAliases();
        // identifier is alias
        if (settings.get("identifier") != null) {
            IndicesAliasesRequestBuilder requestBuilder = ingest.client().admin().indices().prepareAliases();
            logger.debug("adding alias {} to index {}", settings.get("identifier"), getIndex());
            requestBuilder.addAlias(getIndex(), settings.get("identifier"));
            requestBuilder.execute().actionGet();
        }
        // for union catalog, create aliases for "main ISILs" using xbib.identifier
        if ("DE-605".equals(settings.get("identifier"))) {
            Map<String, String> sigel2isil = ValueMaps.getAssocStringMap(getClass().getClassLoader(),
                    settings.get("sigel2isil", "/org/xbib/analyzer/mab/sigel2isil.json"), "sigel2isil");
            final List<String> newAliases = newLinkedList();
            final List<String> switchedAliases = newLinkedList();
            IndicesAliasesRequestBuilder requestBuilder = ingest.client().admin().indices().prepareAliases();
            for (String isil : sigel2isil.values()) {
                // only one (or none) hyphen = "main ISIL"
                if (isil.indexOf("-") == isil.lastIndexOf("-")) {
                    GetAliasesResponse getAliasesResponse = ingest.client().admin().indices().prepareGetAliases(isil).execute().actionGet();
                    if (getAliasesResponse.getAliases().isEmpty()) {
                        requestBuilder.addAlias(concreteIndex, isil, FilterBuilders.termsFilter("xbib.identifier", isil));
                        newAliases.add(isil);
                    } else for (ObjectCursor<String> indexName : getAliasesResponse.getAliases().keys()) {
                        if (indexName.value.startsWith(getIndex())) {
                            requestBuilder.removeAlias(indexName.value, isil)
                                    .addAlias(concreteIndex, isil, FilterBuilders.termsFilter("xbib.identifier", isil));
                            switchedAliases.add(isil);
                        }
                    }
                }
            }
            if (!newAliases.isEmpty() || !switchedAliases.isEmpty()) {
                requestBuilder.execute().actionGet();
                logger.info("{} new aliases created, {} aliases switched", newAliases.size(), switchedAliases.size());
            } else {
                logger.warn("no new or switched aliases");
            }
        }
    }

    protected MARCEntityQueue createQueue(Map<String,Object> params) {
        return new MyQueue(params);
    }

    protected abstract void process(InputStream in, MARCEntityQueue queue) throws IOException;

    class MyQueue extends MARCEntityQueue {

        public MyQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.marc.bib"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements",  "/org/xbib/analyzer/marc/bib.json")
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(IRINamespaceContext.getInstance(),
                    getConcreteIndex(), getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordNumber(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{}", builder.string());
            }
            if (executor != null) {
                // tell executor we increased document count by one
                executor.metric().mark();
                if (executor.metric().count() % 10000 == 0) {
                    try {
                        writeMetrics(executor.metric(), null);
                    } catch (Exception e) {
                        throw new IOException("metric failed", e);
                    }
                }
            }
        }
    }
}
