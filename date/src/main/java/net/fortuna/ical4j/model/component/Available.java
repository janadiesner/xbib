
package net.fortuna.ical4j.model.component;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.util.PropertyValidator;

/**
 *
 * Defines an iCalendar Available component.
 *
 * <pre>
 *
 *       availablec  = &quot;BEGIN&quot; &quot;:&quot; &quot;AVAILABLE&quot; CRLF
 *
 *                    availableprop
 *
 *                    &quot;END&quot; &quot;:&quot; &quot;AVAILABLE&quot; CRLF
 *
          availableprop  = *(

                         ; the following are REQUIRED,
                         ; but MUST NOT occur more than once

                         dtstamp / dtstart / uid /

                         ; either a 'dtend' or a 'duration' is required
                         ; in a 'availableprop', but 'dtend' and
                         ; 'duration' MUST NOT occur in the same
                         ; 'availableprop', and each MUST NOT occur more
                         ; than once

                         dtend / duration /

                         ; the following are OPTIONAL,
                         ; but MUST NOT occur more than once

                         created / last-mod / recurid / rrule /
                         summary /

                         ; the following are OPTIONAL,
                         ; and MAY occur more than once

                         categories / comment / contact / exdate /
                         rdate / x-prop

                         )
 * </pre>
 *
 */
public class Available extends Component {

    /**
     * Default constructor.
     */
    public Available() {
        super(AVAILABLE);
    }

    /**
     * Constructor.
     * @param properties a list of properties
     */
    public Available(final PropertyList properties) {
        super(AVAILABLE, properties);
    }

    public final void validate(final boolean recurse)
            throws ValidationException {

        /*
         * ; dtstamp / dtstart / uid are required, but MUST NOT occur more than once /
         */
        PropertyValidator.getInstance().assertOne(Property.DTSTART,
                getProperties());
        PropertyValidator.getInstance().assertOne(Property.DTSTAMP,
                getProperties());
        PropertyValidator.getInstance().assertOne(Property.UID,
                getProperties());

        /*       If specified, the "DTSTART" and "DTEND" properties in
         *      "VAVAILABILITY" components and "AVAILABLE" sub-components MUST be
         *      "DATE-TIME" values specified as either date with UTC time or date
         *      with local time and a time zone reference.
         */
        final DtStart start = (DtStart) getProperty(Property.DTSTART);
        if (Value.DATE.equals(start.getParameter(Parameter.VALUE))) {
            throw new ValidationException("Property [" + Property.DTSTART
                    + "] must be a " + Value.DATE_TIME);
        }

        /*
         *                ; the following are optional,
         *                ; but MUST NOT occur more than once
         *
         *               created / last-mod / recurid / rrule /
         *               summary /
         */
        PropertyValidator.getInstance().assertOneOrLess(Property.CREATED,
                getProperties());
        PropertyValidator.getInstance().assertOneOrLess(Property.LAST_MODIFIED,
                getProperties());
        PropertyValidator.getInstance().assertOneOrLess(Property.RECURRENCE_ID,
                getProperties());
        PropertyValidator.getInstance().assertOneOrLess(Property.RRULE,
                getProperties());
        PropertyValidator.getInstance().assertOneOrLess(Property.SUMMARY,
                getProperties());

        /*
         ; either a 'dtend' or a 'duration' is required
         ; in a 'availableprop', but 'dtend' and
         ; 'duration' MUST NOT occur in the same
         ; 'availableprop', and each MUST NOT occur more
         ; than once
         */
        if (getProperty(Property.DTEND) != null) {
            PropertyValidator.getInstance().assertOne(Property.DTEND,
                    getProperties());
            /* Must be DATE_TIME */
            final DtEnd end = (DtEnd) getProperty(Property.DTEND);
            if (Value.DATE.equals(end.getParameter(Parameter.VALUE))) {
                throw new ValidationException("Property [" + Property.DTEND
                        + "] must be a " + Value.DATE_TIME);
            }
        } else {
            PropertyValidator.getInstance().assertOne(Property.DURATION,
                    getProperties());
        }

        /*
         * ; the following are optional, ; and MAY occur more than once
         *               categories / comment / contact / exdate /
         *               rdate / x-prop
         */

        if (recurse) {
            validateProperties();
        }
    }
}
