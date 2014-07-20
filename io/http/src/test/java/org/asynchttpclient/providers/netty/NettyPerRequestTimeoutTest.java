package org.asynchttpclient.providers.netty;

import static org.testng.Assert.*;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.async.PerRequestTimeoutTest;

public class NettyPerRequestTimeoutTest extends PerRequestTimeoutTest {
    
    @Override
    protected void checkTimeoutMessage(String message) {
        assertTrue(message
                .startsWith("Request reached time out of 100 ms after "));
    }
    
    @Override
    public AsyncHttpClient getAsyncHttpClient(AsyncHttpClientConfig config) {
        return NettyProviderUtil.nettyProvider(config);
    }
}
