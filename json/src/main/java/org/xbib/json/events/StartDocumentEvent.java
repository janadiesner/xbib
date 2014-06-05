package org.xbib.json.events;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;

public class StartDocumentEvent extends JsonReaderXmlEvent {

    public StartDocumentEvent(Location location) {
        this.location = location;
    }

    @Override
    public boolean isStartDocument() {
        return true;
    }

    @Override
    public int getEventType() {
        return XMLStreamConstants.START_DOCUMENT;
    }

    @Override
    public String toString() {
        return "StartDocumentEvent()";
    }
}