package org.asynchttpclient.websocket;

/**
 * Default WebSocketListener implementation.  Most methods are no-ops.  This 
 * allows for quick override customization without clutter of methods that the
 * developer isn't interested in dealing with.
 */
public class DefaultWebSocketListener implements  WebSocketByteListener, WebSocketTextListener, WebSocketPingListener, WebSocketPongListener {

    protected WebSocket webSocket;
    
    @Override
    public void onMessage(byte[] message) {
    }

    @Override
    public void onFragment(byte[] fragment, boolean last) {
    }

    @Override
    public void onPing(byte[] message) {
    }

    @Override
    public void onPong(byte[] message) {
    }
    
    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onFragment(String fragment, boolean last) {
    }

    @Override
    public void onOpen(WebSocket websocket) {
        this.webSocket = websocket;
    }

    @Override
    public void onClose(WebSocket websocket) {
        this.webSocket = null;
    }

    @Override
    public void onError(Throwable t) {
    }
}
