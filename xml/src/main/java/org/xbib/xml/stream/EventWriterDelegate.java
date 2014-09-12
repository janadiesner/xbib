package org.xbib.xml.stream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Abstract class for implementing XML filters. This class provides methods that
 * merely delegate to a contained XMLEventWriter. Subclasses should override
 * some of these methods, and may also provide additional methods and fields.
 */
public abstract class EventWriterDelegate implements XMLEventWriter {

    protected EventWriterDelegate(XMLEventWriter out) {
        this.out = out;
    }

    /**
     * The downstream writer, to which events are delegated.
     */
    protected final XMLEventWriter out;

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        out.setNamespaceContext(context);
    }

    public NamespaceContext getNamespaceContext() {
        return out.getNamespaceContext();
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        out.setDefaultNamespace(uri);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        out.setPrefix(prefix, uri);
    }

    public String getPrefix(String uri) throws XMLStreamException {
        return out.getPrefix(uri);
    }

    public void add(XMLEvent event) throws XMLStreamException {
        out.add(event);
    }

    /**
     * Add events from the given reader, one by one.
     */
    public void add(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            add(reader.nextEvent());
        }
    }

    public void flush() throws XMLStreamException {
        out.flush();
    }

    public void close() throws XMLStreamException {
        out.close();
    }

}