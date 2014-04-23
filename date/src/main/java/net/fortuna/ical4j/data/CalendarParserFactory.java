
package net.fortuna.ical4j.data;

import net.fortuna.ical4j.util.Configurator;

/**
 * Provides access to the configured {@link net.fortuna.ical4j.data.CalendarParser} instance. Alternative factory implementations may be
 * specified via the following system property:
 * 
 * <pre>
 * net.fortuna.ical4j.parser=&lt;factory_class_name&gt;
 * </pre>
 */
public abstract class CalendarParserFactory {

    /**
     * The system property used to specify an alternate {@link net.fortuna.ical4j.data.CalendarParser} implementation.
     */
    public static final String KEY_FACTORY_CLASS = "net.fortuna.ical4j.parser";

    private static CalendarParserFactory instance;
    static {
        try {
            final Class factoryClass = Class.forName(
                    Configurator.getProperty(KEY_FACTORY_CLASS));
            instance = (CalendarParserFactory) factoryClass.newInstance();
        }
        catch (Exception e) {
            instance = new DefaultCalendarParserFactory();
        }
    }

    /**
     * @return a shared factory instance
     */
    public static CalendarParserFactory getInstance() {
        return instance;
    }

    /**
     * Returns a new instance of the configured {@link net.fortuna.ical4j.data.CalendarParser}.
     * @return a calendar parser instance
     */
    public abstract CalendarParser createParser();

}
