package org.xbib.marc.xml;

import org.testng.annotations.Test;
import org.xbib.StreamUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xml.sax.InputSource;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static org.testng.Assert.assertEquals;

public class MarcXchangeContentHandlerTest {

    private final Logger logger = LoggerFactory.getLogger(MarcXchangeContentHandlerTest.class.getName());

    @Test
    public void testMarcXchangeListener() throws Exception {
        final StringBuilder sb = new StringBuilder();
        InputStream in = getClass().getResourceAsStream("zdb-sru-marcxmlplus.xml");
        if (in == null) {
            throw new IOException("input stream not found");
        }
        MarcXchangeContentHandler handler = new MarcXchangeContentHandler() {

            @Override
            public void leader(String label) {
                logger.debug("leader="+label);
                sb.append("leader").append("\n");
                sb.append(label).append("\n");
            }

            @Override
            public void beginRecord(String format, String type) {
                logger.debug("beginRecord format="+format + " type="+type);
                sb.append("beginRecord").append("\n");
                sb.append(format).append("\n");
                sb.append(type).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                logger.debug("beginControlField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                logger.debug("endControlField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                logger.debug("beginDataField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                logger.debug("endDataField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                logger.debug("beginSubField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                logger.debug("endsubField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endRecord() {
                logger.debug("endRecord");
                sb.append("endRecord").append("\n");
            }

        };

        MarcXchangeReader reader = new MarcXchangeReader();
        reader.setHandler(handler);
        InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
        reader.parse(source);
        in.close();

        InputStreamReader r = new InputStreamReader(getClass().getResourceAsStream("zdb-sru-marcxmlplus.txt"));
        StringWriter w = new StringWriter();
        StreamUtil.copy(r, w);
        assertEquals(sb.toString(), w.toString());

    }
}
