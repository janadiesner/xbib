package org.xbib.stream.exceptions;

import org.xbib.stream.Stream;

/**
 * A failure that occurs when a {@link Stream} cannot be opened.
 */
public class StreamOpenException extends StreamException {

    /**
     * Creates an instance with a given cause.
     *
     * @param cause the cause
     */
    public StreamOpenException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance with a given message and a given cause.
     *
     * @param msg   the message
     * @param cause the cause
     */
    public StreamOpenException(String msg, Throwable cause) {
        super(msg, cause);
    }


}
