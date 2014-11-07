package org.xbib.marc.xml.stream;

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertNull;

public class MarcXchangeEventConsumerTest extends StreamTester {

    @Test
    public void testMarcXchangeEventConsumer() throws Exception {
        File file = File.createTempFile("HT016424175-event.", ".xml");
        FileWriter sw = new FileWriter(file);
        MarcXchangeWriter writer = new MarcXchangeWriter(sw);
        writer.setFormat("AlephXML").setType("Bibliographic");
        writer.startDocument();
        writer.beginCollection();

        MarcXchangeReader consumer = new MarcXchangeReader()
                .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                .setMarcXchangeListener(writer);
        try (InputStream in = getClass().getResourceAsStream("HT016424175.xml")) {
            XMLEventReader xmlReader = XMLInputFactory.newInstance().createXMLEventReader(in);
            while (xmlReader.hasNext()) {
                XMLEvent event = xmlReader.nextEvent();
                consumer.add(event);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        writer.endCollection();
        writer.endDocument();
        sw.close();
        assertNull(writer.getException());
        sw.close();
        assertStream(getClass().getResource("HT016424175-event.xml").openStream(),
                new FileInputStream(file));
    }
}
