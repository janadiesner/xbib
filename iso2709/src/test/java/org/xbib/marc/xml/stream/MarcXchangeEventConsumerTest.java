package org.xbib.marc.xml.stream;

import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertNull;

public class MarcXchangeEventConsumerTest {

    private final Logger logger = LoggerFactory.getLogger(MarcXchangeEventConsumerTest.class.getName());

    private final XMLInputFactory factory = XMLInputFactory.newInstance();

    @Test
    public void testMarcXchangeEventConsumer() throws Exception {

        new File("target").mkdirs();
        FileWriter sw = new FileWriter("target/HT016424175-event.xml");
        //StringWriter sw = new StringWriter();
        MarcXchangeWriter writer = new MarcXchangeWriter(sw);
        writer.setFormat("AlephXML").setType("Bibliographic");
        writer.startDocument();
        writer.beginCollection();

        MarcXchangeReader consumer = new MarcXchangeReader()
                .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                .setMarcXchangeListener(writer);
        try (InputStream in = getClass().getResourceAsStream("HT016424175.xml")) {
            XMLEventReader xmlReader = factory.createXMLEventReader(in);
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

        logger.error("err?", writer.getException());

    }
}
