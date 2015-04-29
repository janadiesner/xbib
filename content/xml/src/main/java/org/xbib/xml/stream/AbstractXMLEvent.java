package org.xbib.xml.stream;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Abstract base class for {@link XMLEvent} implementations.
 */
public abstract class AbstractXMLEvent implements XMLEvent {

    /**
     * The event location.
     */
    protected Location location;

    /**
     * The schema type.
     */
    protected QName schemaType;

    public AbstractXMLEvent() {
    }

    public Location getLocation() {
        return location;
    }

    public QName getSchemaType() {
        return schemaType;
    }

    public Characters asCharacters() {
        return (Characters) this;
    }

    public EndElement asEndElement() {
        return (EndElement) this;
    }

    public StartElement asStartElement() {
        return (StartElement) this;
    }

    public boolean isAttribute() {
        return getEventType() == ATTRIBUTE;
    }

    public boolean isCharacters() {
        switch (getEventType()) {
            case CHARACTERS:
            case SPACE:
            case CDATA:
                return true;
            default:
                return false;
        }
    }

    public boolean isEndDocument() {
        return getEventType() == END_DOCUMENT;
    }

    public boolean isEndElement() {
        return getEventType() == END_ELEMENT;
    }

    public boolean isEntityReference() {
        return getEventType() == ENTITY_REFERENCE;
    }

    public boolean isNamespace() {
        return getEventType() == NAMESPACE;
    }

    public boolean isProcessingInstruction() {
        return getEventType() == PROCESSING_INSTRUCTION;
    }

    public boolean isStartDocument() {
        return getEventType() == START_DOCUMENT;
    }

    public boolean isStartElement() {
        return getEventType() == START_ELEMENT;
    }

    public abstract void writeAsEncodedUnicode(Writer writer) throws XMLStreamException;

    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            this.writeAsEncodedUnicode(writer);
        } catch (XMLStreamException e) {
            // shouldn't happen?
        }
        return writer.toString();
    }

}