package org.asynchttpclient.async;

import static org.testng.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * Tests default asynchronous life cycle.
 * 
 */
public abstract class AsyncStreamLifecycleTest extends AbstractBasicTest {
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @AfterClass
    @Override
    public void tearDownGlobal() throws Exception {
        super.tearDownGlobal();
        executorService.shutdownNow();
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new AbstractHandler() {
            public void handle(String s, Request request, HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
                resp.setContentType("text/plain;charset=utf-8");
                resp.setStatus(200);
                final Continuation continuation = ContinuationSupport.getContinuation(req);
                continuation.suspend();
                final PrintWriter writer = resp.getWriter();
                executorService.submit(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            logger.error("Failed to sleep for 100 ms.", e);
                        }
                        logger.info("Delivering part1.");
                        writer.write("part1");
                        writer.flush();
                    }
                });
                executorService.submit(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            logger.error("Failed to sleep for 200 ms.", e);
                        }
                        logger.info("Delivering part2.");
                        writer.write("part2");
                        writer.flush();
                        continuation.complete();
                    }
                });
                request.setHandled(true);
            }
        };
    }

    // TODO Netty only.

    @Test(groups = { "standalone", "default_provider" })
    public void testStream() throws IOException {
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
            assertEquals(queue.size(), 2);
            assertTrue(queue.contains("part1"));
            assertTrue(queue.contains("part2"));
            assertTrue(status.get());
            assertEquals(headers.get(), 1);
        } finally {
            ahc.close();
        }
    }
}
