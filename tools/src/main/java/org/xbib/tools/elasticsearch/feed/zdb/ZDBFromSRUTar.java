
package org.xbib.tools.elasticsearch.feed.zdb;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.Packet;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.dialects.AbstractTarReader;
import org.xbib.marc.xml.MarcXmlEventConsumer;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineException;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.tools.elasticsearch.Feeder;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.text.Normalizer;

public class ZDBFromSRUTar extends Feeder {


    private final static Logger logger = LoggerFactory.getLogger(ZDBFromSRUTar.class.getName());

    public static void main(String[] args) {
        try {
            new ZDBFromSRUTar()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private ZDBFromSRUTar() {
    }


    @Override
    protected ZDBFromSRUTar prepare(Ingest output) {
        return this;
    }

    protected ZDBFromSRUTar prepare() throws IOException {
        super.prepare();
        return this;
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new ZDBFromSRUTar();
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {

        String bibIndex = settings.get("bibIndex", "zdb");
        String bibType = settings.get("bibType", "title");
        String holIndex = settings.get("holIndex", "zdbholdings");
        String holType = settings.get("holType", "holdings");

        final OurElementOutput bibout = new OurElementOutput().setIndex(bibIndex).setType(bibType);

        final OurElementOutput holout = new OurElementOutput().setIndex(holIndex).setType(holType);

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

        final MyTarReader reader = new MyTarReader()
                .setURI(uri)
                .setListener(handler);
        while (reader.hasNext()) {
            reader.next();
        }
        reader.close();
        bibmapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown bib keys={}", bibmapper.unknownKeys());
        }
        holmapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown hol keys={}", holmapper.unknownKeys());
        }
        logger.info("sink counter = {}", sink.getCounter());
    }

    private class MyTarReader extends AbstractTarReader {

        private final XMLInputFactory factory = XMLInputFactory.newInstance();

        private MarcXchangeListener listener;

        public MyTarReader setURI(URI uri) {
            super.setURI(uri);
            return this;
        }

        public MyTarReader setListener(MarcXchangeListener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        protected void process(Packet packet) throws IOException {
            MarcXmlEventConsumer consumer = new MarcXmlEventConsumer();
            consumer.setListener(listener);
            StringReader sr = new StringReader(packet.toString());
            try {
                XMLEventReader xmlReader = factory.createXMLEventReader(sr);
                while (xmlReader.hasNext()) {
                    consumer.add(xmlReader.nextEvent());
                }
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void error(Pipeline pipeline, PipelineRequest request, PipelineException error) {
            logger.error(error.getMessage(), error);
        }
    }

    private class OurElementOutput extends CountableElementOutput<ResourceContext, Resource> {

        String index;

        String type;

        public OurElementOutput setIndex(String index) {
            this.index = index;
            return this;
        }

        public OurElementOutput setType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            IRI iri = context.resource().id();
            context.resource().id(IRI.builder().scheme("http").host(index).query(type).fragment(iri.getFragment()).build());
            sink.output(context, contentBuilder);
        }
    }

}
