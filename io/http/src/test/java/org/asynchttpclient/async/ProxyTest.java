package org.asynchttpclient.async;

import static org.testng.Assert.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.ProxyServer;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

/**
 * Proxy usage tests.
 * 
 */
public abstract class ProxyTest extends AbstractBasicTest {
    private class ProxyHandler extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                response.addHeader("target", r.getHttpURI().getPath());
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // this handler is to handle POST request
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            r.setHandled(true);
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new ProxyHandler();
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testRequestLevelProxy() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            String target = "http://127.0.0.1:1234/";
            Future<Response> f = client.prepareGet(target).setProxyServer(new ProxyServer("127.0.0.1", port1)).execute();
            Response resp = f.get(3, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("target"), "/");
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testGlobalProxy() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        AsyncHttpClientConfig cfg = new AsyncHttpClientConfig.Builder().setProxyServer(new ProxyServer("127.0.0.1", port1)).build();
        AsyncHttpClient client = getAsyncHttpClient(cfg);
        try {
            String target = "http://127.0.0.1:1234/";
            Future<Response> f = client.prepareGet(target).execute();
            Response resp = f.get(3, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("target"), "/");
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testBothProxies() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        AsyncHttpClientConfig cfg = new AsyncHttpClientConfig.Builder().setProxyServer(new ProxyServer("127.0.0.1", port1 - 1)).build();
        AsyncHttpClient client = getAsyncHttpClient(cfg);
        try {
            String target = "http://127.0.0.1:1234/";
            Future<Response> f = client.prepareGet(target).setProxyServer(new ProxyServer("127.0.0.1", port1)).execute();
            Response resp = f.get(3, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("target"), "/");
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testNonProxyHosts() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        AsyncHttpClientConfig cfg = new AsyncHttpClientConfig.Builder().setProxyServer(new ProxyServer("127.0.0.1", port1 - 1)).build();
        AsyncHttpClient client = getAsyncHttpClient(cfg);
        try {

            String target = "http://127.0.0.1:1234/";
            client.prepareGet(target).setProxyServer(new ProxyServer("127.0.0.1", port1).addNonProxyHost("127.0.0.1")).execute().get();
            assertFalse(true);
        } catch (Throwable e) {
            assertNotNull(e.getCause());
            assertEquals(e.getCause().getClass(), ConnectException.class);
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testNonProxyHostIssue202() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            String target = "http://127.0.0.1:" + port1 + "/";
            Future<Response> f = client.prepareGet(target).setProxyServer(new ProxyServer("127.0.0.1", port1 - 1).addNonProxyHost("127.0.0.1")).execute();
            Response resp = f.get(3, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("target"), "/");
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void runSequentiallyBecauseNotThreadSafe() throws Exception {
        testProxyProperties();
        testIgnoreProxyPropertiesByDefault();
        testProxyActivationProperty();
        testWildcardNonProxyHosts();
        testUseProxySelector();
    }

    // @Test(groups = { "standalone", "default_provider" })
    public void testProxyProperties() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        Properties originalProps = System.getProperties();
        try {
            Properties props = new Properties();
            props.putAll(originalProps);

            // FIXME most likely non threadsafe!
            System.setProperties(props);

            System.setProperty("http.proxyHost", "127.0.0.1");
            System.setProperty("http.proxyPort", String.valueOf(port1));
            System.setProperty("http.nonProxyHosts", "localhost");

            AsyncHttpClientConfig cfg = new AsyncHttpClientConfig.Builder().setUseProxyProperties(true).build();
            AsyncHttpClient client = getAsyncHttpClient(cfg);
            try {
                String target = "http://127.0.0.1:1234/";
                Future<Response> f = client.prepareGet(target).execute();
                Response resp = f.get(3, TimeUnit.SECONDS);
                assertNotNull(resp);
                assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
                assertEquals(resp.getHeader("target"), "/");

                target = "http://localhost:1234/";
                f = client.prepareGet(target).execute();
                try {
                    resp = f.get(3, TimeUnit.SECONDS);
                    fail("should not be able to connect");
                } catch (ExecutionException e) {
                    // ok, no proxy used
                }
            } finally {
                client.close();
            }
        } finally {
            System.setProperties(originalProps);
        }
    }

    // @Test(groups = { "standalone", "default_provider" })
    public void testIgnoreProxyPropertiesByDefault() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        Properties originalProps = System.getProperties();
        try {
            Properties props = new Properties();
            props.putAll(originalProps);

            // FIXME not threadsafe!
            System.setProperties(props);

            System.setProperty("http.proxyHost", "127.0.0.1");
            System.setProperty("http.proxyPort", String.valueOf(port1));
            System.setProperty("http.nonProxyHosts", "localhost");

            AsyncHttpClient client = getAsyncHttpClient(null);
            try {
                String target = "http://127.0.0.1:1234/";
                Future<Response> f = client.prepareGet(target).execute();
                try {
                    f.get(3, TimeUnit.SECONDS);
                    fail("should not be able to connect");
                } catch (ExecutionException e) {
                    // ok, no proxy used
                }
            } finally {
                client.close();
            }
        } finally {
            System.setProperties(originalProps);
        }
    }

    // @Test(groups = { "standalone", "default_provider" })
    public void testProxyActivationProperty() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        Properties originalProps = System.getProperties();
        try {
            Properties props = new Properties();
            props.putAll(originalProps);

            // FIXME not threadsafe!
            System.setProperties(props);

            System.setProperty("http.proxyHost", "127.0.0.1");
            System.setProperty("http.proxyPort", String.valueOf(port1));
            System.setProperty("http.nonProxyHosts", "localhost");
            System.setProperty("org.asynchttpclient.AsyncHttpClientConfig.useProxyProperties", "true");

            AsyncHttpClient client = getAsyncHttpClient(null);
            try {
                String target = "http://127.0.0.1:1234/";
                Future<Response> f = client.prepareGet(target).execute();
                Response resp = f.get(3, TimeUnit.SECONDS);
                assertNotNull(resp);
                assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
                assertEquals(resp.getHeader("target"), "/");

                target = "http://localhost:1234/";
                f = client.prepareGet(target).execute();
                try {
                    resp = f.get(3, TimeUnit.SECONDS);
                    fail("should not be able to connect");
                } catch (ExecutionException e) {
                    // ok, no proxy used
                }
            } finally {
                client.close();
            }
        } finally {
            System.setProperties(originalProps);
        }
    }

    // @Test(groups = { "standalone", "default_provider" })
    public void testWildcardNonProxyHosts() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        Properties originalProps = System.getProperties();
        try {
            Properties props = new Properties();
            props.putAll(originalProps);

            // FIXME not threadsafe!
            System.setProperties(props);

            System.setProperty("http.proxyHost", "127.0.0.1");
            System.setProperty("http.proxyPort", String.valueOf(port1));
            System.setProperty("http.nonProxyHosts", "127.*");

            AsyncHttpClientConfig cfg = new AsyncHttpClientConfig.Builder().setUseProxyProperties(true).build();
            AsyncHttpClient client = getAsyncHttpClient(cfg);
            try {
                String target = "http://127.0.0.1:1234/";
                Future<Response> f = client.prepareGet(target).execute();
                try {
                    f.get(3, TimeUnit.SECONDS);
                    fail("should not be able to connect");
                } catch (ExecutionException e) {
                    // ok, no proxy used
                }
            } finally {
                client.close();
            }
        } finally {
            System.setProperties(originalProps);
        }
    }

    // @Test(groups = { "standalone", "default_provider" })
    public void testUseProxySelector() throws IOException, ExecutionException, TimeoutException, InterruptedException {
        ProxySelector originalProxySelector = ProxySelector.getDefault();
        try {
            ProxySelector.setDefault(new ProxySelector() {
                public List<Proxy> select(URI uri) {
                    if (uri.getHost().equals("127.0.0.1")) {
                        return Arrays.asList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", port1)));
                    } else {
                        return Arrays.asList(Proxy.NO_PROXY);
                    }
                }

                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                }
            });

            AsyncHttpClientConfig cfg = new AsyncHttpClientConfig.Builder().setUseProxySelector(true).build();
            AsyncHttpClient client = getAsyncHttpClient(cfg);
            try {
                String target = "http://127.0.0.1:1234/";
                Future<Response> f = client.prepareGet(target).execute();
                Response resp = f.get(3, TimeUnit.SECONDS);
                assertNotNull(resp);
                assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
                assertEquals(resp.getHeader("target"), "/");

                target = "http://localhost:1234/";
                f = client.prepareGet(target).execute();
                try {
                    f.get(3, TimeUnit.SECONDS);
                    fail("should not be able to connect");
                } catch (ExecutionException e) {
                    // ok, no proxy used
                }
            } finally {
                client.close();
            }
        } finally {
            // FIXME not threadsafe
            ProxySelector.setDefault(originalProxySelector);
        }
    }
}
