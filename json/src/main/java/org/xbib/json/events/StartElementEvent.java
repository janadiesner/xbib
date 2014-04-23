package org.xbib.json.events;

import org.xbib.json.events.JsonReaderXmlEvent;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;

public class StartElementEvent extends JsonReaderXmlEvent {

    public StartElementEvent(QName name, Location location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public boolean isStartElement() {
        return true;
    }

    @Override
    public int getEventType() {
        return XMLStreamConstants.START_ELEMENT;
    }

    @Override
    public String toString() {
        return "StartElementEvent(" + name + ")";
    }
}