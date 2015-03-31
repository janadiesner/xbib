package org.semarglproject.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semarglproject.jsonld.JsonLdReader;
import org.semarglproject.rdf.NQuadsSerializer;
import org.semarglproject.sink.CharOutputSink;
import org.semarglproject.source.StreamProcessor;
import org.testng.annotations.Test;
import org.xbib.io.StreamUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import static org.testng.Assert.assertEquals;

public class JsonLdParserTest {

    private final static Logger logger = LogManager.getLogger(JsonLdParserTest.class);

    @Test
    public void testToRdf() throws Exception {
        //int i = 16;
        for (int i = 1; i < 120; i++) {
            InputStream in = getClass().getResourceAsStream(String.format("/json-ld-org/toRdf-%04d-in.jsonld", i));
            if (in != null) {
                CharOutputSink charOutputSink = new CharOutputSink();
                charOutputSink.connect(System.err);
                StreamProcessor streamProcessor = new StreamProcessor(JsonLdReader.connect(NQuadsSerializer.connect(charOutputSink)));
                String uri = String.format("http://json-ld.org/test-suite/tests/toRdf-%04d-in.jsonld", i);
                Reader input = new InputStreamReader(in, "UTF-8");
                //File f = new File(String.format("/tmp/test-%04d.jsonld", i));
                Writer writer = //new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
                        new StringWriter();
                charOutputSink.connect(writer);
                streamProcessor.process(input, uri);
                input.close();
                writer.close();
                InputStream in2 = getClass().getResourceAsStream(String.format("/json-ld-org/toRdf-%04d-out.nq", i));
                StringWriter writer2 = new StringWriter();
                StreamUtil.copy(new InputStreamReader(in2, "UTF-8"), writer2);
                logger.info("{} == {}", writer.toString(), writer2.toString());
                assertEquals(writer.toString().trim(), writer2.toString().trim());
                logger.info("i={}", i);
            }
        }
    }

}