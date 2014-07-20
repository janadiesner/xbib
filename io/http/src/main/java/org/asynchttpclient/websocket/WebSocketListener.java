package org.asynchttpclient.websocket;

/**
 * A generic {@link org.asynchttpclient.websocket.WebSocketListener} for WebSocket events. Use the appropriate listener for receiving message bytes.
 */
public interface WebSocketListener {

    /**
     * Invoked when the {@link org.asynchttpclient.websocket.WebSocket} is open.
     *
     * @param websocket websocket
     */
    void onOpen(WebSocket websocket);

    /**
     * Invoked when the {@link org.asynchttpclient.websocket.WebSocket} is close.
     *
     * @param websocket websocket
     */
    void onClose(WebSocket websocket);

    /**
     * Invoked when the {@link org.asynchttpclient.websocket.WebSocket} is open.
     *
     * @param t a {@link Throwable}
     */
    void onError(Throwable t);

}
