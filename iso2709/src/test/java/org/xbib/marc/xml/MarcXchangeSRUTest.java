package org.xbib.marc.xml;

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.Field;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.InputStream;

public class MarcXchangeSRUTest extends StreamTester {

    @Test
    public void testMarcXchangeListener() throws Exception {
        final StringBuilder sb = new StringBuilder();
        MarcXchangeContentHandler handler = new MarcXchangeContentHandler() {
            @Override
            public void beginCollection() {
            }

            @Override
            public void endCollection() {
            }

            @Override
            public void beginRecord(String format, String type) {
                sb.append("beginRecord").append("\n");
                sb.append(format).append("\n");
                sb.append(type).append("\n");
            }

            @Override
            public void leader(String label) {
                sb.append("leader").append("\n");
                sb.append(label).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endRecord() {
                sb.append("endRecord").append("\n");
            }

        };

        InputStream in = getClass().getResource("zdb-sru-marcxmlplus.xml").openStream();
        MarcXchangeReader reader = new MarcXchangeReader(in);
        reader.setContentHandler(handler);
        reader.parse();
        in.close();

        FileWriter fw = new FileWriter("zdb-sru-marcxmlplus.txt");
        fw.write(sb.toString());
        fw.close();

        assertStream(getClass().getResource("zdb-sru-marcxmlplus.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
    }
}
