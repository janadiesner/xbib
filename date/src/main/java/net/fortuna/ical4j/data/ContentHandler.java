
package net.fortuna.ical4j.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * Implementors provide functionality applicable during the parsing of an iCalendar data stream (e.g. building an object
 * model).
 */
public interface ContentHandler {

    /**
     * Triggers the start of handling a calendar.
     */
    void startCalendar();

    /**
     * Triggers the end of handling a calendar.
     */
    void endCalendar();

    /**
     * Triggers the start of handling a component.
     * @param name a component name
     */
    void startComponent(String name);

    /**
     * Triggers the end of handling a component.
     * @param name a component name
     */
    void endComponent(String name);

    /**
     * Triggers the start of handling a property.
     * @param name a property name
     */
    void startProperty(String name);

    /**
     * Triggers the handling of a property value.
     * @param value a property value
     * @throws java.net.URISyntaxException where the property value is not a valid URI for applicable properties
     * @throws java.text.ParseException where the date value cannot be parsed for applicable properties
     * @throws java.io.IOException where data cannot be read for applicable properties
     */
    void propertyValue(String value) throws URISyntaxException, ParseException,
            IOException;

    /**
     * Triggers the end of handling a property.
     * @param name a property name
     */
    void endProperty(String name);

    /**
     * Triggers the handling of a parameter.
     * @param name a parameter name
     * @param value a parameter value
     * @throws java.net.URISyntaxException where the parameter value is not a valid URI for applicable parameters
     */
    void parameter(String name, String value) throws URISyntaxException;
}
