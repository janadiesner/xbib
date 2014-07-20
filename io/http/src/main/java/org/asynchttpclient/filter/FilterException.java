package org.asynchttpclient.filter;

/**
 * An exception that can be thrown by an {@link org.asynchttpclient.AsyncHandler} to interrupt invocation of
 * the {@link org.asynchttpclient.filter.RequestFilter} and {@link org.asynchttpclient.filter.ResponseFilter}. It also interrupt the request and response processing.
 */
public class FilterException extends Exception {

    /**
     * @param message
     */
    public FilterException(final String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FilterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
