package org.asynchttpclient.providers.netty;

import static org.testng.Assert.*;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.ConnectionsPool;
import org.asynchttpclient.async.ConnectionPoolTest;

public class NettyConnectionPoolTest extends ConnectionPoolTest {

    @Override
    public AsyncHttpClient getAsyncHttpClient(AsyncHttpClientConfig config) {
        return NettyProviderUtil.nettyProvider(config);
    }

    @Override
    public void testInvalidConnectionsPool() {
        ConnectionsPool<String, Channel> cp = new ConnectionsPool<String, Channel>() {

            public boolean offer(String key, Channel connection) {
                return false;
            }

            public Channel poll(String connection) {
                return null;
            }

            public boolean removeAll(Channel connection) {
                return false;
            }

            public boolean canCacheConnection() {
                return false;
            }

            public void destroy() {

            }
        };

        AsyncHttpClient client = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setConnectionsPool(cp).build());
        try {
            Exception exception = null;
            try {
                client.prepareGet(getTargetUrl()).execute().get(TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception ex) {
                ex.printStackTrace();
                exception = ex;
            }
            assertNotNull(exception);
            assertEquals(exception.getMessage(), "Too many connections -1");
        } finally {
            client.close();
        }
    }

    @Override
    public void testValidConnectionsPool() {
        ConnectionsPool<String, Channel> cp = new ConnectionsPool<String, Channel>() {

            public boolean offer(String key, Channel connection) {
                return true;
            }

            public Channel poll(String connection) {
                return null;
            }

            public boolean removeAll(Channel connection) {
                return false;
            }

            public boolean canCacheConnection() {
                return true;
            }

            public void destroy() {

            }
        };

        AsyncHttpClient client = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setConnectionsPool(cp).build());
        try {
            Exception exception = null;
            try {
                client.prepareGet(getTargetUrl()).execute().get(TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception ex) {
                ex.printStackTrace();
                exception = ex;
            }
            assertNull(exception);
        } finally {
            client.close();
        }
    }
}
