
package net.fortuna.ical4j.data;

import java.text.MessageFormat;

/**
 * An exception thrown when an error occurs in parsing iCalendar data.
 */
public class ParserException extends Exception {

    private static final String ERROR_MESSAGE_PATTERN = "Error at line {0}:";

    private int lineNo;

    /**
     * @param lineNo line number where parsing error ocurred
     */
    public ParserException(final int lineNo) {
        this.lineNo = lineNo;
    }

    /**
     * Constructor with message.
     * @param message a descriptive message for the exception
     * @param lineNo line number where parsing error ocurred
     */
    public ParserException(final String message, final int lineNo) {
        super(MessageFormat.format(ERROR_MESSAGE_PATTERN, new Object[] { new Integer(lineNo)}) + message);
        this.lineNo = lineNo;
    }

    /**
     * Constructor with message and cause.
     * @param message a descriptive message for the exception
     * @param lineNo line number where parsing error ocurred
     * @param cause a throwable that is the cause of this exception
     */
    public ParserException(final String message, final int lineNo,
            final Throwable cause) {

        super(MessageFormat.format(ERROR_MESSAGE_PATTERN, new Object[] { new Integer(lineNo)}) + message, cause);
        this.lineNo = lineNo;
    }

    /**
     * @return the lineNo
     */
    public final int getLineNo() {
        return lineNo;
    }
}
