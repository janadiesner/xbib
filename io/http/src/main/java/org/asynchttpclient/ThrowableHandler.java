package org.asynchttpclient;

/**
 * Simple {@link Throwable} handler to be used with {@link org.asynchttpclient.SimpleAsyncHttpClient}
 */
public interface ThrowableHandler {

    void onThrowable(Throwable t);

}
