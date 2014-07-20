package org.asynchttpclient.websocket;

/**
 * A {@link org.asynchttpclient.websocket.WebSocketListener} for text message
 */
public interface WebSocketTextListener extends WebSocketListener {

    /**
     * Invoked when WebSocket text message are received.
     * @param message a {@link String} message
     */
    void onMessage(String message);

    /**
     * Invoked when WebSocket text fragments are received.
     *
     * @param fragment text fragment
     * @param last if this fragment is the last of the series.
     */
    void onFragment(String fragment, boolean last);

}
