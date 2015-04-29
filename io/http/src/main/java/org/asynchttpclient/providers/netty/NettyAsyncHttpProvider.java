package org.asynchttpclient.providers.netty;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.AsyncHttpProvider;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.providers.netty.channel.Channels;
import org.asynchttpclient.providers.netty.handler.NettyChannelHandler;
import org.asynchttpclient.providers.netty.request.NettyRequestSender;

public class NettyAsyncHttpProvider implements AsyncHttpProvider {

    private static final Logger logger = LogManager.getLogger(NettyAsyncHttpProvider.class.getName());

    private final AsyncHttpClientConfig config;
    private final NettyAsyncHttpProviderConfig nettyConfig;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Channels channels;
    private final NettyRequestSender requestSender;

    public NettyAsyncHttpProvider(AsyncHttpClientConfig config) {

        this.config = config;
        nettyConfig = config.getAsyncHttpProviderConfig() instanceof NettyAsyncHttpProviderConfig ? //
        NettyAsyncHttpProviderConfig.class.cast(config.getAsyncHttpProviderConfig())
                : new NettyAsyncHttpProviderConfig();
        channels = new Channels(config, nettyConfig);
        requestSender = new NettyRequestSender(closed, config, channels);
        NettyChannelHandler channelHandler = new NettyChannelHandler(config, nettyConfig, requestSender, channels, closed);
        channels.configure(channelHandler);
    }

    @Override
    public String toString() {
        return String.format("NettyAsyncHttpProvider4:\n\t- maxConnections: %d\n\t- openChannels: %s\n\t- connectionPools: %s", config.getMaxTotalConnections()
                - channels.freeConnections.availablePermits(), channels.openChannels.toString(), channels.connectionsPool.toString());
    }

    @Override
    public void close() {
        closed.set(true);
        try {
            channels.close();
            config.reaper().shutdown();
        } catch (Throwable t) {
            logger.warn("Unexpected error on close", t);
        }
    }

    @Override
    public <T> ListenableFuture<T> execute(Request request, final AsyncHandler<T> asyncHandler) throws IOException {
        return requestSender.sendRequest(request, asyncHandler, null, nettyConfig.isAsyncConnect(), false);
    }
}
