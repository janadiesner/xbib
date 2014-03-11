package org.xbib.marc.dialects;

import org.testng.annotations.Test;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.io.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.MarcXmlEventConsumer;
import org.xbib.rdf.Resource;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.util.IntervalIterator;

import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;

public class MarcXmlTarReaderTest {

    private final Logger logger = LoggerFactory.getLogger(AlephPublishReaderTest.class.getName());

    @Test
    public void testMABTarImport() throws Exception {
        final CountableElementOutput<MABContext,Resource> output = new CountableElementOutput<MABContext,Resource>() {
            @Override
            public void output(MABContext context, ContentBuilder contentBuilder) throws IOException {
                counter.incrementAndGet();
            }
        };

        ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.marc.dialects.alephtest");
        String uriStr = bundle.getString("uri");
        Integer from = Integer.parseInt(bundle.getString("from"));
        Integer to = Integer.parseInt(bundle.getString("to"));

        final MABElementBuilderFactory builderFactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(output);
            }
        };
        final MABElementMapper mapper = new MABElementMapper("mab/hbz/tit")
                .start(builderFactory);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                    @Override
                    public KeyValueStreamAdapter<FieldCollection, String> keyValue(FieldCollection key, String value) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("begin");
                            for (Field f : key) {
                                logger.trace("tag={} ind={} subf={} data={}",
                                        f.tag(), f.indicator(), f.subfieldId(), f.data());
                            }
                            logger.trace("end");
                        }
                        return this;
                    }

                });
        final MarcXmlEventConsumer consumer = new MarcXmlEventConsumer()
                .setListener(kv);
        new MarcXmlTarReader()
                .setIterator(new IntervalIterator(from, to))
                .setURI(URI.create(uriStr))
                .setEventConsumer(consumer);

        logger.info("counter = {}", output.getCounter());
        // TODO assert
    }
    
}
