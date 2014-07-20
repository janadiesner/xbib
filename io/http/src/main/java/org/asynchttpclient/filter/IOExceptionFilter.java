package org.asynchttpclient.filter;

/**
 * This filter is invoked when an {@link java.io.IOException} occurs during an http transaction.
 */
public interface IOExceptionFilter {

    /**
     * An {@link org.asynchttpclient.AsyncHttpProvider} will invoke {@link org.asynchttpclient.filter.IOExceptionFilter#filter} and will
     * use the returned {@link org.asynchttpclient.filter.FilterContext} to replay the {@link org.asynchttpclient.Request} or abort the processing.
     *
     * @param ctx a {@link org.asynchttpclient.filter.FilterContext}
     * @return {@link org.asynchttpclient.filter.FilterContext}. The {@link org.asynchttpclient.filter.FilterContext} instance may not the same as the original one.
     * @throws org.asynchttpclient.filter.FilterException to interrupt the filter processing.
     */
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException;
}
