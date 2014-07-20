package org.asynchttpclient.websocket;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.UpgradeHandler;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link AsyncHandler} which is able to execute WebSocket upgrade. Use the Builder for configuring WebSocket options.
 */
public class WebSocketUpgradeHandler implements UpgradeHandler<WebSocket>, AsyncHandler<WebSocket> {

    private WebSocket webSocket;
    private final ConcurrentLinkedQueue<WebSocketListener> l;
    @SuppressWarnings("unused")
    private final String protocol;
    @SuppressWarnings("unused")
    private final long maxByteSize;
    @SuppressWarnings("unused")
    private final long maxTextSize;
    private final AtomicBoolean ok = new AtomicBoolean(false);
    private final AtomicBoolean onSuccessCalled = new AtomicBoolean(false);

    private WebSocketUpgradeHandler(Builder b) {
        l = b.l;
        protocol = b.protocol;
        maxByteSize = b.maxByteSize;
        maxTextSize = b.maxTextSize;
    }

    @Override
    public final void onThrowable(Throwable t) {
        onFailure(t);
    }

    public boolean touchSuccess(){
        return onSuccessCalled.getAndSet(true);
    }

    @Override
    public final STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        return STATE.CONTINUE;
    }

    @Override
    public final STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        if (responseStatus.getStatusCode() == 101) {
            return STATE.UPGRADE;
        } else {
            return STATE.ABORT;
        }
    }

    @Override
    public final STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
        return STATE.CONTINUE;
    }

    @Override
    public final WebSocket onCompleted() throws Exception {
        if (webSocket == null) {
            throw new IllegalStateException("WebSocket is null");
        }
        return webSocket;
    }

    @Override
    public final void onSuccess(WebSocket webSocket) {
        this.webSocket = webSocket;
        for (WebSocketListener w : l) {
            webSocket.addWebSocketListener(w);
            w.onOpen(webSocket);
        }
        ok.set(true);
    }

    @Override
    public final void onFailure(Throwable t) {
        for (WebSocketListener w : l) {
            if (!ok.get() && webSocket != null) {
                webSocket.addWebSocketListener(w);
            }
            w.onError(t);
        }
    }

    public final void onClose(WebSocket webSocket, int status, String reasonPhrase) {
        // Connect failure
        if (this.webSocket == null) this.webSocket = webSocket;

        for (WebSocketListener w : l) {
            if (webSocket != null) {
                webSocket.addWebSocketListener(w);
            }
            w.onClose(webSocket);
            if (w instanceof WebSocketCloseCodeReasonListener) {
                WebSocketCloseCodeReasonListener.class.cast(w).onClose(webSocket, status, reasonPhrase);
            }
        }
    }

    /**
     * Build a {@link org.asynchttpclient.websocket.WebSocketUpgradeHandler}
     */
    public final static class Builder {
        private ConcurrentLinkedQueue<WebSocketListener> l = new ConcurrentLinkedQueue<WebSocketListener>();
        private String protocol = "";
        private long maxByteSize = 8192;
        private long maxTextSize = 8192;

        /**
         * Add a {@link org.asynchttpclient.websocket.WebSocketListener} that will be added to the {@link org.asynchttpclient.websocket.WebSocket}
         *
         * @param listener a {@link org.asynchttpclient.websocket.WebSocketListener}
         * @return this
         */
        public Builder addWebSocketListener(WebSocketListener listener) {
            l.add(listener);
            return this;
        }

        /**
         * Remove a {@link org.asynchttpclient.websocket.WebSocketListener}
         *
         * @param listener a {@link org.asynchttpclient.websocket.WebSocketListener}
         * @return this
         */
        public Builder removeWebSocketListener(WebSocketListener listener) {
            l.remove(listener);
            return this;
        }

        /**
         * Set the WebSocket protocol.
         *
         * @param protocol the WebSocket protocol.
         * @return this
         */
        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Set the max size of the WebSocket byte message that will be sent.
         *
         * @param maxByteSize max size of the WebSocket byte message
         * @return this
         */
        public Builder setMaxByteSize(long maxByteSize) {
            this.maxByteSize = maxByteSize;
            return this;
        }

        /**
         * Set the max size of the WebSocket text message that will be sent.
         *
         * @param maxTextSize max size of the WebSocket byte message
         * @return this
         */
        public Builder setMaxTextSize(long maxTextSize) {
            this.maxTextSize = maxTextSize;
            return this;
        }

        /**
         * Build a {@link org.asynchttpclient.websocket.WebSocketUpgradeHandler}
         * @return a {@link org.asynchttpclient.websocket.WebSocketUpgradeHandler}
         */
        public WebSocketUpgradeHandler build() {
            return new WebSocketUpgradeHandler(this);
        }
    }
}
