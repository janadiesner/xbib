package org.asynchttpclient.providers.netty;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.AsyncHttpProviderConfig;
import org.asynchttpclient.async.AsyncProvidersBasicTest;

public class NettyAsyncProviderBasicTest extends AsyncProvidersBasicTest {

    @Override
    public AsyncHttpClient getAsyncHttpClient(AsyncHttpClientConfig config) {
        return NettyProviderUtil.nettyProvider(config);
    }

    @Override
    protected AsyncHttpProviderConfig<?, ?> getProviderConfig() {
        final NettyAsyncHttpProviderConfig config = new NettyAsyncHttpProviderConfig();
        config.addProperty("TCP_NODELAY", true);
        return config;
    }
}
