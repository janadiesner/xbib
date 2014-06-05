
package org.xbib.tools.feed.elasticsearch.zdb;

import org.xbib.rdf.context.CountableContextResourceOutput;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.Packet;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.xml.MarcXmlEventConsumer;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.content.ContentBuilder;
import org.xbib.tools.Feeder;
import org.xbib.tools.util.AbstractTarReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.text.Normalizer;

/**
 * Index SRU data
 */
public class FromSRUTar extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(FromSRUTar.class.getName());

    protected FromSRUTar prepare() throws IOException {
        super.prepare();
        return this;
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromSRUTar();
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
            context.getResource().id(IRI.builder().scheme("http").host(index).query(type).fragment(iri.getFragment()).build());
            sink.output(context, resource, contentBuilder);
        }
    }

}
