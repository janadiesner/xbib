package org.xbib.xml.stream;

import javax.xml.stream.events.Characters;

/**
 * Abstract base class for various {@link Characters} implementations.
 */
public abstract class AbstractCharactersEvent extends AbstractXMLEvent implements Characters {

    protected String data;

    public AbstractCharactersEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public boolean isCharacters() {
        return true;
    }

    public boolean isWhiteSpace() {
        String data = getData();
        for (int i = 0, s = data.length(); i < s; i++) {
            switch (data.charAt(i)) {
                case ' ':
                case '\n':
                case '\t':
                case '\r':
                    continue;
                default:
                    return false;
            }
        }
        return true;

    }

}