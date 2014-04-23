
package net.fortuna.ical4j.data;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

/**
 * Implementors provide iCalendar parsing functionality.
 */
public interface CalendarParser {

    /**
     * Parse the iCalendar data from the specified input stream.
     * @param in an input stream from which to read iCalendar data
     * @param handler the content handler to notify during parsing
     * @throws java.io.IOException thrown when unable to read from the specified stream
     * @throws net.fortuna.ical4j.data.ParserException thrown if an error occurs during parsing
     */
    void parse(InputStream in, ContentHandler handler) throws IOException,
            ParserException;

    /**
     * Parse the iCalendar data from the specified reader.
     * @param in a reader from which to read iCalendar data
     * @param handler the content handler to notify during parsing
     * @throws java.io.IOException thrown when unable to read from the specified reader
     * @throws net.fortuna.ical4j.data.ParserException thrown if an error occurs during parsing
     */
    void parse(Reader in, ContentHandler handler) throws IOException,
            ParserException;
}
