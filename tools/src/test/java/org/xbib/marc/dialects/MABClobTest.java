package org.xbib.marc.dialects;

import org.testng.annotations.Test;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.io.Packet;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.marc.xml.MarcXmlEventConsumer;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineException;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.xcontent.ContentBuilder;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileWriter;
import java.io.IOException;

import java.io.StringReader;
import java.net.URI;

public class MABClobTest {

    private final Logger logger = LoggerFactory.getLogger(MABClobTest.class.getName());

    // this takes many minutes!

    @Test
    public void testMABElements() throws Exception {
        logger.debug("testMABELements starting");

        URI uri = URI.create("file:" + System.getProperty("user.home") + "/import/hbz/vk/clob-aleph-all-20140126.tar.gz");

        FileWriter rdf = new FileWriter("target/hbz.nt");
        final NTripleWriter writer  = new NTripleWriter()
                        .output(rdf);
        final CountableElementOutput output = new CountableElementOutput<ResourceContext, Resource>() {

            @Override
            public void output(ResourceContext context, ContentBuilder<ResourceContext, Resource> builder) throws IOException {
                writer.write(context.resource());
                counter.incrementAndGet();
            }

        };
        final MABElementBuilderFactory mabElementBuilderfactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder()
                        .addOutput(output);
            }
        };
        final MABElementMapper mapper = new MABElementMapper("mab/hbz/tit")
                .pipelines(Runtime.getRuntime().availableProcessors())
                .detectUnknownKeys(true)
                .start(mabElementBuilderfactory);
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(mapper);

        final MarcXchangeContentHandler handler = new MarcXchangeContentHandler()
                .addListener("Bibliographic", kv);

        final MyTarReader reader = new MyTarReader()
                .setURI(uri)
                .setListener(handler);
        while (reader.hasNext()) {
            reader.next();
        }
        reader.close();
        mapper.close();
        logger.info("counter={}", output.getCounter());

        writer.close();
        rdf.close();

        FileWriter w = new FileWriter("mab-hbz-unknown.txt");
        w.append(mapper.unknownKeys().toString());
        w.close();
        w = new FileWriter("mab-hbz-dump.txt");
        mapper.dump("mab/hbz/test", w);
        w.close();
        logger.debug("testMABELements ending");
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
}
