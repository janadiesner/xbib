
package net.fortuna.ical4j.data;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

/**
 * Writes an iCalendar model to an output stream.
 */
public class CalendarOutputter extends AbstractOutputter {

    /**
     * Default constructor.
     */
    public CalendarOutputter() {
        super();
    }

    /**
     * @param validating indicates whether to validate calendar when outputting to stream
     */
    public CalendarOutputter(final boolean validating) {
        super(validating);
    }

    /**
     * @param validating indicates whether to validate calendar when outputting to stream
     * @param foldLength maximum number of characters before a line is folded
     */
    public CalendarOutputter(final boolean validating, final int foldLength) {
        super(validating, foldLength);
    }

    /**
     * Outputs an iCalender string to the specified output stream.
     * @param calendar calendar to write to ouput stream
     * @param out an output stream
     * @throws java.io.IOException thrown when unable to write to output stream
     * @throws net.fortuna.ical4j.model.ValidationException where calendar validation fails
     */
    public final void output(final Calendar calendar, final OutputStream out)
            throws IOException, ValidationException {
        output(calendar, new OutputStreamWriter(out, DEFAULT_CHARSET));
    }

    /**
     * Outputs an iCalender string to the specified writer.
     * @param calendar calendar to write to writer
     * @param out a writer
     * @throws java.io.IOException thrown when unable to write to writer
     * @throws net.fortuna.ical4j.model.ValidationException where calendar validation fails
     */
    public final void output(final Calendar calendar, final Writer out)
            throws IOException, ValidationException {
        if (isValidating()) {
            calendar.validate();
        }

        final FoldingWriter writer = new FoldingWriter(out, foldLength);
        try {
            writer.write(calendar.toString());
        }
        finally {
            writer.close();
        }
    }
}
