package org.xbib.marc.addons;

import java.io.IOException;
import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABContext;
import org.xbib.elements.ElementMapper;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.marc.MarcXchange2KeyValue;

import java.net.URI;
import java.util.concurrent.ExecutionException;

public class ConcurrentMABTarReaderTest {
        
    public void test() throws InterruptedException, ExecutionException {
        ImporterFactory factory = new ImporterFactory() {

            @Override
            public Importer newImporter() {
                return createImporter();
            }
        };
        new ImportService().setThreads(4).setFactory(factory).execute();
    }
    
    private Importer createImporter() {
        ElementOutput<MABContext> output = new ElementOutput<MABContext>() {
            long counter;

            @Override
            public void enabled(boolean enabled) {
                
            }
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(MABContext context) throws IOException {
                counter++;
            }

            @Override
            public long getCounter() {
                return counter;
            }
            
        };        
        MABBuilder builder = new MABBuilder().addOutput(output);
        ElementMapper mapper = new ElementMapper("mab").addBuilder(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        return new MABTarReader()
                .setURI(URI.create("tarbz2:///Users/joerg/Downloads/clobs.hbz.metadata.mab.alephxml-clob-dump3"))
                .setListener(kv);
    }
    
}