package org.xbib.json.events;

import org.xbib.json.events.JsonReaderXmlEvent;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;

public class EndDocumentEvent  extends JsonReaderXmlEvent {

    public EndDocumentEvent(Location location) {
        this.location = location;
    }

    @Override
    public boolean isEndDocument() {
        return true;
    }

    @Override
    public int getEventType() {
        return XMLStreamConstants.END_DOCUMENT;
    }

    @Override
    public String toString() {
        return "EndDocumentEvent()";
    }
}