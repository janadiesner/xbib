package org.asynchttpclient.listener;

import org.asynchttpclient.FluentCaseInsensitiveStringsMap;

import java.io.IOException;

/**
 * A simple interface an application can implements in order to received byte transfer information.
 */
public interface TransferListener {

    /**
     * Invoked when the request bytes are starting to get send.
     */
    public void onRequestHeadersSent(FluentCaseInsensitiveStringsMap headers);

    /**
     * Invoked when the response bytes are starting to get received.
     */
    public void onResponseHeadersReceived(FluentCaseInsensitiveStringsMap headers);

    /**
     * Invoked every time response's chunk are received.
     *
     * @param bytes a {@link byte[]}
     */
    public void onBytesReceived(byte[] bytes) throws IOException;

    /**
     * Invoked every time request's chunk are sent.
     *
     * @param amount  The amount of bytes to transfer
     * @param current The amount of bytes transferred
     * @param total   The total number of bytes transferred
     */
    public void onBytesSent(long amount, long current, long total);

    /**
     * Invoked when the response bytes are been fully received.
     */
    public void onRequestResponseCompleted();

    /**
     * Invoked when there is an unexpected issue.
     *
     * @param t a {@link Throwable}
     */
    public void onThrowable(Throwable t);
}
