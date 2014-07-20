package org.asynchttpclient.filter;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Request;

import java.io.IOException;

/**
 * A {@link org.asynchttpclient.filter.FilterContext} can be used to decorate {@link Request} and {@link AsyncHandler} from a list of {@link RequestFilter}.
 * {@link RequestFilter} gets executed before the HTTP request is made to the remote server. Once the response bytes are
 * received, a {@link org.asynchttpclient.filter.FilterContext} is then passed to the list of {@link ResponseFilter}. {@link ResponseFilter}
 * gets invoked before the response gets processed, e.g. before authorization, redirection and invocation of {@link AsyncHandler}
 * gets processed.
 * <p/>
 * Invoking {@link org.asynchttpclient.filter.FilterContext#getResponseStatus()} returns an instance of {@link HttpResponseStatus}
 * that can be used to decide if the response processing should continue or not. You can stop the current response processing
 * and replay the request but creating a {@link org.asynchttpclient.filter.FilterContext}. The {@link org.asynchttpclient.AsyncHttpProvider}
 * will interrupt the processing and "replay" the associated {@link Request} instance.
 */
public class FilterContext<T> {

    private final FilterContextBuilder<T> b;

    /**
     * Create a new {@link org.asynchttpclient.filter.FilterContext}
     *
     * @param b a {@link org.asynchttpclient.filter.FilterContext.FilterContextBuilder}
     */
    private FilterContext(FilterContextBuilder<T> b) {
        this.b = b;
    }

    /**
     * Return the original or decorated {@link AsyncHandler}
     *
     * @return the original or decorated {@link AsyncHandler}
     */
    public AsyncHandler<T> getAsyncHandler() {
        return b.asyncHandler;
    }

    /**
     * Return the original or decorated {@link Request}
     *
     * @return the original or decorated {@link Request}
     */
    public Request getRequest() {
        return b.request;
    }

    /**
     * Return the unprocessed response's {@link HttpResponseStatus}
     *
     * @return the unprocessed response's {@link HttpResponseStatus}
     */
    public HttpResponseStatus getResponseStatus() {
        return b.responseStatus;
    }

    /**
     * Return the response {@link HttpResponseHeaders}
     */
    public HttpResponseHeaders getResponseHeaders() {
        return b.headers;
    }

    /**
     * Return true if the current response's processing needs to be interrupted and a new {@link Request} be executed.
     *
     * @return true if the current response's processing needs to be interrupted and a new {@link Request} be executed.
     */
    public boolean replayRequest() {
        return b.replayRequest;
    }

    /**
     * Return the {@link java.io.IOException}
     *
     * @return the {@link java.io.IOException}
     */
    public IOException getIOException() {
        return b.ioException;
    }

    public static class FilterContextBuilder<T> {
        private AsyncHandler<T> asyncHandler = null;
        private Request request = null;
        private HttpResponseStatus responseStatus = null;
        private boolean replayRequest = false;
        private IOException ioException = null;
        private HttpResponseHeaders headers;

        public FilterContextBuilder() {
        }

        public FilterContextBuilder(FilterContext<T> clone) {
            asyncHandler = clone.getAsyncHandler();
            request = clone.getRequest();
            responseStatus = clone.getResponseStatus();
            replayRequest = clone.replayRequest();
            ioException = clone.getIOException();
        }

        public AsyncHandler<T> getAsyncHandler() {
            return asyncHandler;
        }

        public FilterContextBuilder<T> asyncHandler(AsyncHandler<T> asyncHandler) {
            this.asyncHandler = asyncHandler;
            return this;
        }

        public Request getRequest() {
            return request;
        }

        public FilterContextBuilder<T> request(Request request) {
            this.request = request;
            return this;
        }

        public FilterContextBuilder<T> responseStatus(HttpResponseStatus responseStatus) {
            this.responseStatus = responseStatus;
            return this;
        }

        public FilterContextBuilder<T> responseHeaders(HttpResponseHeaders headers) {
            this.headers = headers;
            return this;
        }

        public FilterContextBuilder<T> replayRequest(boolean replayRequest) {
            this.replayRequest = replayRequest;
            return this;
        }

        public FilterContextBuilder<T> ioException(IOException ioException) {
            this.ioException = ioException;
            return this;
        }

        public FilterContext<T> build() {
            return new FilterContext<T>(this);
        }
    }

}
