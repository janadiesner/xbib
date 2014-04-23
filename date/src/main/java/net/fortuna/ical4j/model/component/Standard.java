
package net.fortuna.ical4j.model.component;

import net.fortuna.ical4j.model.PropertyList;

/**
 * Defines an iCalendar standard timezone observance component.
 *
 * <pre>
 *
 *       standardc  = &quot;BEGIN&quot; &quot;:&quot; &quot;STANDARD&quot; CRLF
 *
 *                    tzprop
 *
 *                    &quot;END&quot; &quot;:&quot; &quot;STANDARD&quot; CRLF
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
 */
public class Standard extends Observance {

    /**
     * Default constructor.
     */
    public Standard() {
        super(STANDARD);
    }

    /**
     * Constructor.
     * @param properties a list of properties
     */
    public Standard(final PropertyList properties) {
        super(STANDARD, properties);
    }
}
