package org.asynchttpclient.async;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;

import static org.testng.Assert.fail;

/**
 * Tests case where response doesn't have body.
 * 
 */
public abstract class EmptyBodyTest extends AbstractBasicTest {
    private class NoBodyResponseHandler extends AbstractHandler {
        public void handle(String s, Request request, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

            if (!req.getMethod().equalsIgnoreCase("PUT")) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(204);
            }
            request.setHandled(true);
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new NoBodyResponseHandler();
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testEmptyBody() throws IOException {
        AsyncHttpClient ahc = getAsyncHttpClient(null);
        try {
            final AtomicBoolean err = new AtomicBoolean(false);
            final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
            final AtomicBoolean status = new AtomicBoolean(false);
            final AtomicInteger headers = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(1);
            ahc.executeRequest(ahc.prepareGet(getTargetUrl()).build(), new AsyncHandler<Object>() {
                public void onThrowable(Throwable t) {
                    fail("Got throwable.", t);
                    err.set(true);
                }

                public STATE onBodyPartReceived(HttpResponseBodyPart e) throws Exception {
                    String s = new String(e.getBodyPartBytes());
                    logger.info("got part: {}", s);
                    if (s.equals("")) {
                        // noinspection ThrowableInstanceNeverThrown
                        logger.warn("Sampling stacktrace.", new Throwable("trace that, we should not get called for empty body."));
                    }
                    queue.put(s);
                    return STATE.CONTINUE;
                }

                public STATE onStatusReceived(HttpResponseStatus e) throws Exception {
                    status.set(true);
                    return STATE.CONTINUE;
                }

                public STATE onHeadersReceived(HttpResponseHeaders e) throws Exception {
                    if (headers.incrementAndGet() == 2) {
                        throw new Exception("Analyze this.");
                    }
                    return STATE.CONTINUE;
                }

                public Object onCompleted() throws Exception {
                    latch.countDown();
                    return null;
                }
            });
            try {
                assertTrue(latch.await(1, TimeUnit.SECONDS), "Latch failed.");
            } catch (InterruptedException e) {
                fail("Interrupted.", e);
            }
            assertFalse(err.get());
            assertEquals(queue.size(), 0);
            assertTrue(status.get());
            assertEquals(headers.get(), 1);
        } finally {
            ahc.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testPutEmptyBody() throws Exception {
        AsyncHttpClient ahc = getAsyncHttpClient(null);
        try {
            Response response = ahc.preparePut(getTargetUrl()).setBody("String").execute().get();

            assertNotNull(response);
            assertEquals(response.getStatusCode(), 204);
            assertEquals(response.getResponseBody(), "");
            assertTrue(response.getResponseBodyAsStream() instanceof InputStream);
            assertEquals(response.getResponseBodyAsStream().read(), -1);
        } finally {
            ahc.close();
        }
    }
}
