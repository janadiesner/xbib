package org.asynchttpclient;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * An {@link AsyncHandler} augmented with an {@link #onCompleted(Response)} convenience method which gets called
 * when the {@link Response} processing is finished.  This class also implement the {@link ProgressAsyncHandler} callback,
 * all doing nothing except returning {@link org.asynchttpclient.AsyncHandler.STATE#CONTINUE}
 *
 * @param <T> Type of the value that will be returned by the associated {@link java.util.concurrent.Future}
 */
public abstract class AsyncCompletionHandler<T> implements AsyncHandler<T>, ProgressAsyncHandler<T> {

    private final Logger log = LoggerFactory.getLogger(AsyncCompletionHandlerBase.class.getName());

    private final Response.ResponseBuilder builder = new Response.ResponseBuilder();

    public STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
        builder.accumulate(content);
        return STATE.CONTINUE;
    }

    public STATE onStatusReceived(final HttpResponseStatus status) throws Exception {
        builder.reset();
        builder.accumulate(status);
        return STATE.CONTINUE;
    }

    public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
        builder.accumulate(headers);
        return STATE.CONTINUE;
    }

    public final T onCompleted() throws Exception {
        return onCompleted(builder.build());
    }

    public void onThrowable(Throwable t) {
        log.debug(t.getMessage(), t);
    }

    /**
     * Invoked once the HTTP response processing is finished.
     * <p/>
     * <p/>
     * Gets always invoked as last callback method.
     *
     * @param response The {@link Response}
     * @return T Value that will be returned by the associated {@link java.util.concurrent.Future}
     * @throws Exception if something wrong happens
     */
    abstract public T onCompleted(Response response) throws Exception;

    /**
     * Invoked when the content (a {@link java.io.File}, {@link String} or {@link java.io.FileInputStream} has been fully
     * written on the I/O socket.
     *
     * @return a {@link org.asynchttpclient.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     */
    public STATE onHeaderWriteCompleted() {
        return STATE.CONTINUE;
    }

    /**
     * Invoked when the content (a {@link java.io.File}, {@link String} or {@link java.io.FileInputStream} has been fully
     * written on the I/O socket.
     *
     * @return a {@link org.asynchttpclient.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     */
    public STATE onContentWriteCompleted() {
        return STATE.CONTINUE;
    }

    /**
     * Invoked when the I/O operation associated with the {@link Request} body as been progressed.
     *
     * @param amount  The amount of bytes to transfer
     * @param current The amount of bytes transferred
     * @param total   The total number of bytes transferred
     * @return a {@link org.asynchttpclient.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     */
    public STATE onContentWriteProgress(long amount, long current, long total) {
        return STATE.CONTINUE;
    }
}
