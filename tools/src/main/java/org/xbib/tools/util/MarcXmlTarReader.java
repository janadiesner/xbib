package org.xbib.tools.util;

import org.xbib.io.Packet;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.xml.MarcXmlEventConsumer;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.element.LongPipelineElement;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

public class MarcXmlTarReader extends AbstractTarReader {

    private final Logger logger = LoggerFactory.getLogger(MarcXmlTarReader.class.getName());

    private final XMLInputFactory factory = XMLInputFactory.newInstance();

    private MarcXchangeListener listener;

    private MarcXmlEventConsumer consumer;

    public MarcXmlTarReader() {
    }

    public MarcXmlTarReader setURI(URI uri) {
        super.setURI(uri);
        return this;
    }

    public MarcXmlTarReader setListener(MarcXchangeListener listener) {
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

    public MarcXmlTarReader setEventConsumer(MarcXmlEventConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public void newRequest(Pipeline<MeterMetric, LongPipelineElement> pipeline, LongPipelineElement request) {
        try {
            StringReader sr = new StringReader(packet.toString());
            XMLEventReader xmlReader = factory.createXMLEventReader(sr);
            while (xmlReader.hasNext()) {
                XMLEvent event = xmlReader.nextEvent();
                if (consumer != null) {
                    consumer.add(event);
                }
            }
        } catch (XMLStreamException e) {
            logger.error(e.getMessage(), e);
        }
    }
}