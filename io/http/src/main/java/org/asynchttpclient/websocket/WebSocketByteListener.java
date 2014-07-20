package org.asynchttpclient.websocket;

/**
 * A {@link org.asynchttpclient.websocket.WebSocketListener} for bytes
 */
public interface WebSocketByteListener extends WebSocketListener {

    /**
     * Invoked when bytes are available.
     * @param message a byte array.
     */
    void onMessage(byte[] message);


    /**
     * Invoked when bytes of a fragmented message are available.
     *
     * @param fragment byte[] fragment.
     * @param last if this fragment is the last in the series.
     */
    void onFragment(byte[] fragment, boolean last);

}
