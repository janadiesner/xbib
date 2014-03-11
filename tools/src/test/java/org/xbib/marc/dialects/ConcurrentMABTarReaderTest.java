package org.xbib.marc.dialects;

import java.io.IOException;

import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.elements.ElementOutput;
import org.xbib.marc.xml.MarcXmlEventConsumer;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.simple.SimplePipelineExecutor;
import org.xbib.pipeline.Pipeline;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;
import org.xbib.rdf.xcontent.ContentBuilder;

import java.net.URI;

public class ConcurrentMABTarReaderTest {

    private MABElementMapper mapper;

    /**
     * Takes a long time (~10-20 minutes!)
     * @throws Exception
     */
    public void testMABTarImport() throws Exception {
        new SimplePipelineExecutor()
                .setConcurrency(Runtime.getRuntime().availableProcessors())
                .setPipelineProvider(new PipelineProvider() {

                    @Override
                    public Pipeline get() {
                        return createImporter();
                    }
                })
                .execute();
        mapper.close();
    }
    
    private Pipeline createImporter() {
        final ElementOutput<MABContext,Resource> output = new CountableElementOutput<MABContext,Resource>() {
            @Override
            public void output(MABContext context, ContentBuilder contentBuilder) throws IOException {
                counter.incrementAndGet();
            }
        };
        final MABElementBuilderFactory builderFactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(output);
            }
        };
        final MABElementMapper mapper = new MABElementMapper("mab").start(builderFactory);
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        final MarcXmlEventConsumer consumer = new MarcXmlEventConsumer()
                .setListener(kv);
        return new MarcXmlTarReader()
                .setURI(URI.create("tarbz2://"+System.getProperty("user.home")+"/import/hbz/aleph/clobs.hbz.metadata.mab.alephxml-clob-dump0"))
                .setEventConsumer(consumer);
    }
    
}
