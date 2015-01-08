package org.xbib.tools;

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
import org.xbib.entities.support.ClasspathURLStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TimewindowFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(TimewindowFeeder.class.getSimpleName());

    private static String index;

    private static String concreteIndex;

    protected void setIndex(String index) {
        this.index = index;
    }

    protected String getIndex() {
        return index;
    }

    protected void setConcreteIndex(String concreteIndex) {
        this.concreteIndex = concreteIndex;
    }

    protected String getConcreteIndex() {
        return concreteIndex;
    }

    @Override
    protected TimewindowFeeder prepare() throws IOException {
        ingest = createIngest();
        String timeWindow = settings.get("timewindow") != null ?
                DateTimeFormat.forPattern(settings.get("timewindow")).print(new DateTime()) : "";
        setConcreteIndex(resolveAlias(getIndex() + timeWindow));
        Pattern pattern = Pattern.compile("^(.*)\\d+$");
        Matcher m = pattern.matcher(getConcreteIndex());
        setIndex(m.matches() ? m.group() : getConcreteIndex());
        logger.info("base index name = {}, concrete index name = {}", getIndex(), getConcreteIndex());
        super.prepare();
        return this;
    }

    @Override
    protected TimewindowFeeder createIndex(String index) throws IOException {
        ingest.newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .build());
        if (ingest.client() != null) {
            ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
            if (settings.getAsBoolean("onlyalias", false)) {
                updateAliases();
                return this;
            }
            try {
                String indexSettings = settings.get("index-settings",
                        "classpath:org/xbib/tools/feed/elasticsearch/settings.json");
                InputStream indexSettingsInput = (indexSettings.startsWith("classpath:") ?
                        new URL(null, indexSettings, new ClasspathURLStreamHandler()) :
                        new URL(indexSettings)).openStream();
                String indexMappings = settings.get("index-mapping",
                        "classpath:org/xbib/tools/feed/elasticsearch/mapping.json");
                InputStream indexMappingsInput = (indexMappings.startsWith("classpath:") ?
                        new URL(null, indexMappings, new ClasspathURLStreamHandler()) :
                        new URL(indexMappings)).openStream();
                ingest.newIndex(getConcreteIndex(), getType(),
                        indexSettingsInput, indexMappingsInput);
                indexSettingsInput.close();
                indexMappingsInput.close();
                ingest.startBulk(getConcreteIndex());
            } catch (Exception e) {
                if (!settings.getAsBoolean("ignoreindexcreationerror", false)) {
                    throw e;
                } else {
                    logger.warn("index creation error, but configured to ignore", e);
                }
            }
        }
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
        if (ingest.client() == null) {
            return;
        }
        String index = getIndex();
        String concreteIndex = getConcreteIndex();
        if (!index.equals(concreteIndex)) {
            IndicesAliasesRequestBuilder requestBuilder = ingest.client().admin().indices().prepareAliases();
            GetAliasesResponse getAliasesResponse = ingest.client().admin().indices().prepareGetAliases(index).execute().actionGet();
            if (getAliasesResponse.getAliases().isEmpty()) {
                logger.info("adding alias {} to index {}", index, concreteIndex);
                requestBuilder.addAlias(concreteIndex, index);
                // identifier is alias
                if (settings.get("identifier") != null) {
                    requestBuilder.addAlias(concreteIndex, settings.get("identifier"));
                }
            } else {
                for (ObjectCursor<String> indexName : getAliasesResponse.getAliases().keys()) {
                    if (indexName.value.startsWith(index)) {
                        logger.info("switching alias {} from index {} to index {}", index, indexName.value, concreteIndex);
                        requestBuilder.removeAlias(indexName.value, index)
                                .addAlias(concreteIndex, index);
                        if (settings.get("identifier") != null) {
                            requestBuilder.removeAlias(indexName.value, settings.get("identifier"))
                                    .addAlias(concreteIndex, settings.get("identifier"));
                        }
                    }
                }
            }
            requestBuilder.execute().actionGet();
        }
    }

}
