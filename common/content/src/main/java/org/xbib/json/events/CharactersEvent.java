package org.xbib.json.events;


import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;

public class CharactersEvent extends JsonReaderXmlEvent {

    public CharactersEvent(String text, Location location) {
        this.text = text;
        this.location = location;
    }

    @Override
    public boolean isCharacters() {
        return true;
    }

    @Override
    public int getEventType() {
        return XMLStreamConstants.CHARACTERS;
    }

    @Override
    public String toString() {
        return "CharactersEvent(" + text + ")";
    }
}