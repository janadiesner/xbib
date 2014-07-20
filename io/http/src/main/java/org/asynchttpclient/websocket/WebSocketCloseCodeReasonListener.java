package org.asynchttpclient.websocket;

/**
 * Extend the normal close listener with one that support the WebSocket's code and reason.
 * @see "http://tools.ietf.org/html/rfc6455#section-5.5.1"
 */
public interface WebSocketCloseCodeReasonListener {

    /**
     * Invoked when the {@link org.asynchttpclient.websocket.WebSocket} is close.
     *
     * @param websocket the websocket
     * @param code code
     * @param reason reason
     */
    void onClose(WebSocket websocket, int code, String reason);
}
