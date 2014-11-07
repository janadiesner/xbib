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
package org.xbib.tools.feed.elasticsearch.zdb;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.rdf.Sink;
import org.xbib.elements.UnmappedKeyListener;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.Request;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.FieldList;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Context;
import org.xbib.rdf.ContextWriter;
import org.xbib.rdf.Resource;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.client.SRUClientFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveListener;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.sru.searchretrieve.SearchRetrieveResponseAdapter;
import org.xbib.tools.Feeder;
import org.xbib.xml.stream.SaxEventConsumer;

import javax.xml.stream.util.XMLEventConsumer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FromSRU extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(FromSRU.class.getName());

    private SRUClient client;

    FromSRU() {
        super();
    }

    FromSRU(boolean b) {
        client = SRUClientFactory.newClient();
    }

    @Override
    public String getName() {
        return "zdb-sru-elasticsearch";
    }

    protected FromSRU prepare() throws IOException {
        prepareInput();
        prepareOutput();
        return this;
    }

    protected void prepareInput() throws IOException {
        // define input: fetch from SRU by number file, each line is an ID
        input = new ConcurrentLinkedQueue<>();
        if (settings.get("numbers") != null) {
            FileInputStream in = new FileInputStream(settings.get("numbers"));
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = r.readLine()) != null) {
                input.add(URI.create(String.format(settings.get("uri"), line)));
            }
            in.close();
        } else {
            input.add(URI.create(settings.get("uri")));
        }
        logger.info("uris = {}", input.size());
    }

    protected void prepareOutput() throws IOException {
        String index = settings.get("index");
        String type = settings.get("type");
        Integer shards = settings.getAsInt("shards", 1);
        Integer replica = settings.getAsInt("replica", 0);
        Integer maxbulkactions = settings.getAsInt("maxbulkactions", 100);
        Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                Runtime.getRuntime().availableProcessors());
        output = createIngest();
        beforeIndexCreation(output);
        output.maxActionsPerBulkRequest(maxbulkactions)
                .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                .newClient(ImmutableSettings.settingsBuilder()
                        .put("cluster.name", settings.get("elasticsearch.cluster"))
                        .put("host", settings.get("elasticsearch.host"))
                        .put("port", settings.getAsInt("elasticsearch.port", 9300))
                        .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                        .build());
        output.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        output.shards(shards).replica(replica).newIndex(index);
        afterIndexCreation(output);
        sink = new Sink(output);
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromSRU(true);
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {

        final String bibIndex = settings.get("bibIndex", "zdb");
        final String bibType = settings.get("bibType", "title");
        final String holIndex = settings.get("holIndex", "zdbholdings");
        final String holType = settings.get("holType", "holdings");

        final OurContextOutput bibout = new OurContextOutput().setIndex(bibIndex).setType(bibType);

        final OurContextOutput holout = new OurContextOutput().setIndex(holIndex).setType(holType);

        final Set<String> unmappedbib = Collections.synchronizedSet(new TreeSet<String>());
        final MARCElementMapper bibmapper = new MARCElementMapper("marc/zdb/bib")
                .pipelines(settings.getAsInt("pipelines", 1))
                .setListener(new UnmappedKeyListener<FieldList>() {
                    @Override
                    public void unknown(FieldList key) {
                        logger.warn("unmapped field {}", key);
                        if ((settings.getAsBoolean("detect", false))) {
                            unmappedbib.add("\"" + key + "\"");
                        }
                    }
                })
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        MARCElementBuilder builder = new MARCElementBuilder();
                        builder.addWriter(bibout);
                        return builder;
                    }
                });

        final Set<String> unmappedhol = Collections.synchronizedSet(new TreeSet<String>());
        final MARCElementMapper holmapper = new MARCElementMapper("marc/zdb/hol")
                .pipelines(settings.getAsInt("pipelines", 1))
                .setListener(new UnmappedKeyListener<FieldList>() {
                    @Override
                    public void unknown(FieldList key) {
                        logger.warn("unmapped field {}", key);
                        if ((settings.getAsBoolean("detect", false))) {
                            unmappedbib.add("\"" + key + "\"");
                        }
                    }
                })
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        MARCElementBuilder builder = new MARCElementBuilder();
                        builder.addWriter(holout);
                        return builder;
                    }
                });

        final MarcXchange2KeyValue bib = new MarcXchange2KeyValue()
                .setStringTransformer(new StringTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(bibmapper);

        final MarcXchange2KeyValue hol = new MarcXchange2KeyValue()
                .setStringTransformer(new StringTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(holmapper);

        final MarcXchangeContentHandler handler = new MarcXchangeContentHandler()
                .addListener("Bibliographic", bib)
                .addListener("Holdings", hol);

        final SearchRetrieveListener listener = new SearchRetrieveResponseAdapter() {

            @Override
            public void onConnect(Request request) {
                logger.info("connect, request = " + request);
            }

            @Override
            public void version(String version) {
                logger.info("version = " + version);
            }

            @Override
            public void numberOfRecords(long numberOfRecords) {
                logger.info("numberOfRecords = " + numberOfRecords);
            }

            @Override
            public void beginRecord() {
                logger.info("begin record");
            }

            @Override
            public void recordSchema(String recordSchema) {
                logger.info("got record schema:" + recordSchema);
            }

            @Override
            public void recordPacking(String recordPacking) {
                logger.info("got recordPacking: " + recordPacking);
            }

            @Override
            public void recordIdentifier(String recordIdentifier) {
                logger.info("got recordIdentifier=" + recordIdentifier);
            }

            @Override
            public void recordPosition(int recordPosition) {
                logger.info("got recordPosition=" + recordPosition);
            }

            @Override
            public XMLEventConsumer recordData() {
                // parse MarcXchange here
                return new SaxEventConsumer(handler);
            }

            @Override
            public XMLEventConsumer extraRecordData() {
                // ignore extra data
                return null;
            }

            @Override
            public void endRecord() {
            }

            @Override
            public void onDisconnect(Request request) {
                logger.info("disconnect, request = " + request);
            }
        };

        StringWriter w = new StringWriter();
        SearchRetrieveRequest request = client.newSearchRetrieveRequest()
                .setURI(uri)
                .addListener(listener);
        SearchRetrieveResponse response = client.searchRetrieve(request).to(w);

    }

    protected FromSRU cleanup() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    private class OurContextOutput implements ContextWriter<Context<Resource>, Resource> {

        String index;

        String type;

        public OurContextOutput setIndex(String index) {
            this.index = index;
            return this;
        }

        public OurContextOutput setType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public void write(Context context) throws IOException {
            IRI iri = context.getResource().id();
            context.getResource().id(IRI.builder()
                    .scheme("http")
                    .host(index)
                    .query(type)
                    .fragment(iri.getFragment())
                    .build());
            sink.write(context);
        }
    }
}
