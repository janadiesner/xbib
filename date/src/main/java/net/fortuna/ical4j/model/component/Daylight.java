
package net.fortuna.ical4j.model.component;

import net.fortuna.ical4j.model.PropertyList;

/**
 *
 * Defines an iCalendar daylight savings timezone observance component.
 *
 * <pre>
 *
 *       daylightc  = &quot;BEGIN&quot; &quot;:&quot; &quot;DAYLIGHT&quot; CRLF
 *
 *                    tzprop
 *
 *                    &quot;END&quot; &quot;:&quot; &quot;DAYLIGHT&quot; CRLF
 *
 *       tzprop     = 3*(
 *
 *                  ; the following are each REQUIRED,
 *                  ; but MUST NOT occur more than once
 *
 *                  dtstart / tzoffsetto / tzoffsetfrom /
 *
 *                  ; the following are optional,
 *                  ; and MAY occur more than once
 *
 *                  comment / rdate / rrule / tzname / x-prop
 *
 *                  )
 * </pre>
 *
 */
public class Daylight extends Observance {

    /**
     * Default constructor.
     */
    public Daylight() {
        super(DAYLIGHT);
    }

    /**
     * Constructor.
     * @param properties a list of properties
     */
    public Daylight(final PropertyList properties) {
        super(DAYLIGHT, properties);
    }
}
