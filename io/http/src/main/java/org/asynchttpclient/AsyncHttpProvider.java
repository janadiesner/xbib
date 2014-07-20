package org.asynchttpclient;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface to be used when implementing custom asynchronous I/O HTTP client.
 */
public interface AsyncHttpProvider extends Closeable {

    /**
     * Execute the request and invoke the {@link org.asynchttpclient.AsyncHandler} when the response arrive.
     *
     * @param handler an instance of {@link org.asynchttpclient.AsyncHandler}
     * @return a {@link ListenableFuture} of Type T.
     * @throws java.io.IOException
     */
    <T> ListenableFuture<T> execute(Request request, AsyncHandler<T> handler) throws IOException;

    /**
     * Close the current underlying TCP/HTTP connection.
     */
    void close();
}
