package org.xbib.xml.stream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Abstract class for writing filtered XML streams. This class provides methods
 * that merely delegate to the contained stream. Subclasses should override some
 * of these methods, and may also provide additional methods and fields.
 */
public abstract class StreamWriterDelegate implements XMLStreamWriter {

    protected StreamWriterDelegate(XMLStreamWriter out) {
        this.out = out;
    }

    protected XMLStreamWriter out;

    public Object getProperty(String name) throws IllegalArgumentException {
        return out.getProperty(name);
    }

    public NamespaceContext getNamespaceContext() {
        return out.getNamespaceContext();
    }

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        out.setNamespaceContext(context);
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        out.setDefaultNamespace(uri);
    }

    public void writeStartDocument() throws XMLStreamException {
        out.writeStartDocument();
    }

    public void writeStartDocument(String version) throws XMLStreamException {
        out.writeStartDocument(version);
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        out.writeStartDocument(encoding, version);
    }

    public void writeDTD(String dtd) throws XMLStreamException {
        out.writeDTD(dtd);
    }

    public void writeProcessingInstruction(String target) throws XMLStreamException {
        out.writeProcessingInstruction(target);
    }

    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        out.writeProcessingInstruction(target, data);
    }

    public void writeComment(String data) throws XMLStreamException {
        out.writeComment(data);
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        out.writeEmptyElement(localName);
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        out.writeEmptyElement(namespaceURI, localName);
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        out.writeEmptyElement(prefix, localName, namespaceURI);
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        out.writeStartElement(localName);
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        out.writeStartElement(namespaceURI, localName);
    }

    public void writeStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        out.writeStartElement(prefix, localName, namespaceURI);
    }

    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        out.writeDefaultNamespace(namespaceURI);
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        out.writeNamespace(prefix, namespaceURI);
    }

    public String getPrefix(String uri) throws XMLStreamException {
        return out.getPrefix(uri);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        out.setPrefix(prefix, uri);
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        out.writeAttribute(localName, value);
    }

    public void writeAttribute(String namespaceURI, String localName, String value)
            throws XMLStreamException {
        out.writeAttribute(namespaceURI, localName, value);
    }

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        out.writeAttribute(prefix, namespaceURI, localName, value);
    }

    public void writeCharacters(String text) throws XMLStreamException {
        out.writeCharacters(text);
    }

    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        out.writeCharacters(text, start, len);
    }

    public void writeCData(String data) throws XMLStreamException {
        out.writeCData(data);
    }

    public void writeEntityRef(String name) throws XMLStreamException {
        out.writeEntityRef(name);
    }

    public void writeEndElement() throws XMLStreamException {
        out.writeEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        out.writeEndDocument();
    }

    public void flush() throws XMLStreamException {
        out.flush();
    }

    public void close() throws XMLStreamException {
        out.close();
    }

}