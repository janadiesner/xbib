package org.xbib.stream.exceptions;

import org.xbib.stream.Stream;

/**
 * A failure that occurs when a {@link Stream} cannot be published.
 */
public class StreamPublishException extends StreamException {

    /**
     * Creates an instance with a given cause.
     *
     * @param cause the cause
     */
    public StreamPublishException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance with a given message and a given cause.
     *
     * @param msg   the message
     * @param cause the cause
     */
    public StreamPublishException(String msg, Throwable cause) {
        super(msg, cause);
    }


}
