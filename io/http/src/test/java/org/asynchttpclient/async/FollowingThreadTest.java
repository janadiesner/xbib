package org.asynchttpclient.async;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Simple stress test for exercising the follow redirect.
 */
public abstract class FollowingThreadTest extends AbstractBasicTest {

    private static final int COUNT = 10;

    @Test(timeOut = 30 * 1000, groups = { "online", "default_provider", "scalability" })
    public void testFollowRedirect() throws IOException, ExecutionException, TimeoutException, InterruptedException {

        final CountDownLatch countDown = new CountDownLatch(COUNT);
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            for (int i = 0; i < COUNT; i++) {
                pool.submit(new Runnable() {

                    private int status;

                    public void run() {
                        final CountDownLatch l = new CountDownLatch(1);
                        final AsyncHttpClient ahc = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setFollowRedirects(true).build());
                        try {
                            ahc.prepareGet("http://www.google.com/").execute(new AsyncHandler<Integer>() {

                                public void onThrowable(Throwable t) {
                                    t.printStackTrace();
                                }

                                public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                                    System.out.println(new String(bodyPart.getBodyPartBytes()));
                                    return STATE.CONTINUE;
                                }

                                public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                                    status = responseStatus.getStatusCode();
                                    System.out.println(responseStatus.getStatusText());
                                    return STATE.CONTINUE;
                                }

                                public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                                    return STATE.CONTINUE;
                                }

                                public Integer onCompleted() throws Exception {
                                    l.countDown();
                                    return status;
                                }
                            });

                            l.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            ahc.close();
                            countDown.countDown();
                        }
                    }
                });
            }
            countDown.await();
        } finally {
            pool.shutdown();
        }
    }
}
