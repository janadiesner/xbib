
package org.xbib.template.handlebars;

import org.xbib.template.handlebars.util.Validate;

/**
 * Useful information about a handlebar error.
 */
public class HandlebarsError {

    /**
     * The error's line number.
     */
    public final int line;

    /**
     * The error's column number.
     */
    public final int column;

    /**
     * The error's problem.
     */
    public final String reason;

    /**
     * The error's evidence.
     */
    public final String evidence;

    /**
     * The file's name.
     */
    public final String filename;

    /**
     * The full error's message.
     */
    public final String message;

    /**
     * Creates a new {@link HandlebarsError}.
     *
     * @param filename The file's name. Required.
     * @param line     The error's line number.
     * @param column   The error's column number.
     * @param reason   The error's reason. Required.
     * @param evidence The error's evidence. Required.
     * @param message  The error's message. Required.
     */
    public HandlebarsError(final String filename, final int line,
                           final int column, final String reason, final String evidence,
                           final String message) {
        this.filename = Validate.notEmpty(filename, "The file's name is required");
        Validate.isTrue(line > 0, "The error's line number must be greather than zero");
        this.line = line;
        Validate.isTrue(column > 0, "The error's column number must be greather than zero");
        this.column = column;
        this.reason = Validate.notEmpty(reason, "The file's reason is required");
        this.evidence = Validate.notEmpty(evidence, "The file's evidence is required");
        this.message = Validate.notEmpty(message, "The file's message is required");
    }

    @Override
    public String toString() {
        return message;
    }
}
