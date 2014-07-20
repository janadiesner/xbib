package org.asynchttpclient.filter;

/**
 * A Filter interface that gets invoked before making an actual request.
 */
public interface RequestFilter {

    /**
     * An {@link org.asynchttpclient.AsyncHttpProvider} will invoke {@link org.asynchttpclient.filter.RequestFilter#filter} and will use the
     * returned {@link org.asynchttpclient.filter.FilterContext#getRequest()} and {@link org.asynchttpclient.filter.FilterContext#getAsyncHandler()} to continue the request
     * processing.
     *
     * @param ctx a {@link org.asynchttpclient.filter.FilterContext}
     * @return {@link org.asynchttpclient.filter.FilterContext}. The {@link org.asynchttpclient.filter.FilterContext} instance may not the same as the original one.
     * @throws FilterException to interrupt the filter processing.
     */
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException;
}
