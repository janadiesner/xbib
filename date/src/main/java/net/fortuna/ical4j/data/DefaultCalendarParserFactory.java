
package net.fortuna.ical4j.data;

/**
 *
 * Default factory implementation for calendar parsers.
 */
public class DefaultCalendarParserFactory extends CalendarParserFactory {

    public CalendarParser createParser() {
        return new CalendarParserImpl();
    }
}
