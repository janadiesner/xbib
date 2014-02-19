
package org.xbib.tools.elasticsearch.feed.zdb;

import org.xbib.elasticsearch.ResourceSink;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.Request;
import org.xbib.io.keyvalue.KeyValueStreamAdapter;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.client.SRUClientFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveListener;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.sru.searchretrieve.SearchRetrieveResponseAdapter;
import org.xbib.tools.elasticsearch.Feeder;
import org.xbib.xml.stream.SaxEventConsumer;

import javax.xml.stream.util.XMLEventConsumer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.text.Normalizer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZDBFromSRU extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(ZDBFromSRU.class.getName());

    private SRUClient client;

    public static void main(String[] args) {
        try {
            new ZDBFromSRU()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    ZDBFromSRU() {
    }

    ZDBFromSRU(boolean b) {
        client = SRUClientFactory.newClient();
    }

    @Override
    protected ZDBFromSRU prepare(Ingest output) {
        return this;
    }

    protected ZDBFromSRU prepare() throws IOException {
        prepareInput();
        prepareOutput();
        return this;
    }

    protected void prepareInput() throws IOException {
        // define input
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
        // define output
        URI esURI = URI.create(settings.get("elasticsearch"));
        String index = settings.get("index");
        String type = settings.get("type");
        Integer shards = settings.getAsInt("shards", 1);
        Integer replica = settings.getAsInt("replica", 0);
        Integer maxbulkactions = settings.getAsInt("maxbulkactions", 100);
        Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                Runtime.getRuntime().availableProcessors());
        output = createIngest();
        prepare(output);
        output.maxActionsPerBulkRequest(maxbulkactions)
                .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                .newClient(esURI);
        output.waitForCluster();
        output.setIndex(index)
                .setType(type)
                .dateDetection(false)
                .shards(shards)
                .replica(replica)
                .newIndex();
        sink = new ResourceSink(output);
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new ZDBFromSRU(true);
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {
        setIndex(settings.get("index"));
        setType(settings.get("type"));
        StringWriter w = new StringWriter();
        SearchRetrieveRequest request = client.newSearchRetrieveRequest()
                    .setURI(uri)
                    .addListener(listener);
        SearchRetrieveResponse response = client.searchRetrieve(request).to(w);

    }

    protected ZDBFromSRU cleanup() {
        bibmapper.close();
        holmapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown bib keys={}", bibmapper.unknownKeys());
            logger.info("unknown hol keys={}", holmapper.unknownKeys());
        }
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    private String index;

    private String type;

    private String id;

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    private final OurElementOutput out = new OurElementOutput();

    private final class OurElementOutput extends CountableElementOutput<ResourceContext, Resource> {

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            String index = getIndex();
            String type = getType();
            String id = getID();
            context.resource().id(IRI.builder().scheme("http")
                    .host(index)
                    .query(type)
                    .fragment(id).build());
            logger.debug("output = {}/{}/{}", index, type, id);
            sink.output(context, contentBuilder);
        }
    }

    final MARCElementMapper bibmapper = new MARCElementMapper("marc/zdb/bib")
            .pipelines(settings.getAsInt("pipelines", 1))
            .detectUnknownKeys(settings.getAsBoolean("detect", false))
            .start(new MARCElementBuilderFactory() {
                public MARCElementBuilder newBuilder() {
                    return new MARCElementBuilder().addOutput(out);
                }
            });

    final MARCElementMapper holmapper = new MARCElementMapper("marc/zdb/hol")
            .pipelines(settings.getAsInt("pipelines", 1))
            .detectUnknownKeys(settings.getAsBoolean("detect", false))
            .start(new MARCElementBuilderFactory() {
                public MARCElementBuilder newBuilder() {
                    return new MARCElementBuilder().addOutput(out);
                }
            });

    final MarcXchange2KeyValue bib = new MarcXchange2KeyValue()
            .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                @Override
                public String transform(String value) {
                    return Normalizer.normalize(value, Normalizer.Form.NFC);
                }
            })
            .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                @Override
                public KeyValueStreamAdapter<FieldCollection, String> begin() {
                    logger.debug("bib begin object");
                    return this;
                }

                @Override
                public KeyValueStreamAdapter<FieldCollection, String> keyValue(FieldCollection key, String value) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("bib begin, spec={}", key.toSpec());
                        for (Field f : key) {
                            logger.debug("bib tag='{}' ind='{}' subf='{}' data='{}'",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data());
                        }
                        if (value != null) {
                            logger.debug("bib value='{}'", value);

                        }
                        logger.debug("bib end");
                    }
                    return this;
                }

                @Override
                public KeyValueStreamAdapter<FieldCollection, String> end() {
                    logger.debug("bib end object");
                    return this;
                }
            })
            .addListener(bibmapper);

    final MarcXchange2KeyValue hol = new MarcXchange2KeyValue()
            .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                @Override
                public String transform(String value) {
                    return Normalizer.normalize(value, Normalizer.Form.NFC);
                }
            })
            .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                @Override
                public KeyValueStreamAdapter<FieldCollection, String> begin() {
                    logger.debug("hol begin object");
                    return this;
                }

                @Override
                public KeyValueStreamAdapter<FieldCollection, String> keyValue(FieldCollection key, String value) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("hol begin, spec = {}", key.toSpec());
                        for (Field f : key) {
                            logger.debug("hol tag='{}' ind='{}' subf='{}' data='{}'",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data());
                        }
                        if (value != null) {
                            logger.debug("hol value='{}'", value);

                        }
                        logger.debug("hol end");
                    }
                    return this;
                }

                @Override
                public KeyValueStreamAdapter<FieldCollection, String> end() {
                    logger.debug("hol end object");
                    return this;
                }
            })
            .addListener(holmapper);

    final MarcXchangeContentHandler marcHandler = new MarcXchangeContentHandler()
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
            setID(recordIdentifier);
        }

        @Override
        public void recordPosition(int recordPosition) {
            logger.info("got recordPosition=" + recordPosition);
        }

        @Override
        public XMLEventConsumer recordData() {
            // parse MarcXchange here
            return new SaxEventConsumer(marcHandler);
        }

        @Override
        public XMLEventConsumer extraRecordData() {
            // ignore extra data
            return null;
        }

        @Override
        public void endRecord() {
            setIndex(settings.get("index"));
            setType(marcHandler.getType());
            setID(marcHandler.getRecordNumber());
            logger.info("end record: {}/{}/{}",
                    marcHandler.getFormat(), marcHandler.getType(), marcHandler.getRecordNumber());
        }

        @Override
        public void onDisconnect(Request request) {
            logger.info("disconnect, request = " + request);
        }
    };

}
