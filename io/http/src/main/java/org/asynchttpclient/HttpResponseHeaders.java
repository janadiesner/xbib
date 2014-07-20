package org.asynchttpclient;


/**
 * A class that represent the HTTP headers.
 */
public abstract class HttpResponseHeaders {

    private final boolean trailingHeaders;

    public HttpResponseHeaders() {
        this.trailingHeaders = false;
    }

    public HttpResponseHeaders(boolean trailingHeaders) {
        this.trailingHeaders = trailingHeaders;
    }

    /**
     * Return the HTTP header
     *
     * @return an {@link org.asynchttpclient.FluentCaseInsensitiveStringsMap}
     */
    abstract public FluentCaseInsensitiveStringsMap getHeaders();

    /**
     * Return true is headers has been received after the response body.
     *
     * @return true is headers has been received after the response body.
     */
    public boolean isTrailingHeadersReceived() {
        return trailingHeaders;
    }
}
