package org.asynchttpclient;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Extended {@link java.util.concurrent.Future}
 *
 * @param <V> Type of the value that will be returned.
 */
public interface ListenableFuture<V> extends Future<V> {

    /**
     * Terminate and if there is no exception, mark this Future as done and release the internal lock.
     */
    void done();

    /**
     * Abort the current processing, and propagate the {@link Throwable} to the {@link org.asynchttpclient.AsyncHandler} or {@link java.util.concurrent.Future}
     *
     * @param t
     */
    void abort(Throwable t);

    /**
     * Touch the current instance to prevent external service to times out.
     */
    void touch();

    /**
     * Write the {@link Request} headers
     */
    boolean getAndSetWriteHeaders(boolean writeHeader);

    /**
     * Write the {@link Request} body
     */
    boolean getAndSetWriteBody(boolean writeBody);

    /**
     * <p>Adds a listener and executor to the ListenableFuture.
     * The listener will be {@linkplain java.util.concurrent.Executor#execute(Runnable) passed
     * to the executor} for execution when the {@code Future}'s computation is
     * {@linkplain java.util.concurrent.Future#isDone() complete}.
     * <p/>
     * <p>There is no guaranteed ordering of execution of listeners, they may get
     * called in the order they were added and they may get called out of order,
     * but any listener added through this method is guaranteed to be called once
     * the computation is complete.
     *
     * @param listener the listener to run when the computation is complete.
     * @param exec     the executor to run the listener in.
     * @return this Future
     * @throws NullPointerException if the executor or listener was null.
     * @throws java.util.concurrent.RejectedExecutionException
     *                              if we tried to execute the listener
     *                              immediately but the executor rejected it.
     */
    ListenableFuture<V> addListener(Runnable listener, Executor exec);
}
