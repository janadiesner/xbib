
package org.xbib.tools.feed.elasticsearch.zdb;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.rdf.ResourceSink;
import org.xbib.rdf.context.CountableContextResourceOutput;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.Request;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.content.ContentBuilder;
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
        beforeIndexCreation(output);
        output.maxActionsPerBulkRequest(maxbulkactions)
                .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                .newClient(esURI);
        output.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        output.shards(shards).replica(replica).newIndex(index);
        afterIndexCreation(output);
        sink = new ResourceSink(output);
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

        String bibIndex = settings.get("bibIndex", "zdb");
        String bibType = settings.get("bibType", "title");
        String holIndex = settings.get("holIndex", "zdbholdings");
        String holType = settings.get("holType", "holdings");

        final OurContextResourceOutput bibout = new OurContextResourceOutput().setIndex(bibIndex).setType(bibType);

        final OurContextResourceOutput holout = new OurContextResourceOutput().setIndex(holIndex).setType(holType);

        final MARCElementMapper bibmapper = new MARCElementMapper("marc/zdb/bib")
                .pipelines(settings.getAsInt("pipelines", 1))
                .detectUnknownKeys(settings.getAsBoolean("detect", false))
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        return new MARCElementBuilder()
                                .addOutput(bibout);
                    }
                });

        final MARCElementMapper holmapper = new MARCElementMapper("marc/zdb/hol")
                .pipelines(settings.getAsInt("pipelines", 1))
                .detectUnknownKeys(settings.getAsBoolean("detect", false))
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        return new MARCElementBuilder().addOutput(holout);
                    }
                });

        final MarcXchange2KeyValue bib = new MarcXchange2KeyValue()
                .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
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

    private class OurContextResourceOutput extends CountableContextResourceOutput<ResourceContext, Resource> {

        String index;

        String type;

        public OurContextResourceOutput setIndex(String index) {
            this.index = index;
            return this;
        }

        public OurContextResourceOutput setType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public void output(ResourceContext context, Resource resource, ContentBuilder contentBuilder) throws IOException {
            IRI iri = context.getResource().id();
            context.getResource().id(IRI.builder()
                    .scheme("http")
                    .host(index)
                    .query(type)
                    .fragment(iri.getFragment())
                    .build());
            sink.output(context, context.getResource(), contentBuilder);
        }
    }
}
