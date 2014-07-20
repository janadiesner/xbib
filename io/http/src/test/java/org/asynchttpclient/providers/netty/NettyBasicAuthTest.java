package org.asynchttpclient.providers.netty;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.async.BasicAuthTest;

public class NettyBasicAuthTest extends BasicAuthTest {

    @Override
    public AsyncHttpClient getAsyncHttpClient(AsyncHttpClientConfig config) {
        return NettyProviderUtil.nettyProvider(config);
    }

    @Override
    public String getProviderClass() {
    	return NettyAsyncHttpProvider.class.getName();
    }
}
