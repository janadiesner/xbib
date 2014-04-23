
package net.fortuna.ical4j.data;

/**
 * A parser factory for the hCal microformat.
 */
public class HCalendarParserFactory extends CalendarParserFactory {

    public CalendarParser createParser() {
        return new HCalendarParser();
    }

}
