package org.asynchttpclient.async;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.testng.Assert.*;

public abstract class RetryRequestTest extends AbstractBasicTest {
    public static class SlowAndBigHandler extends AbstractHandler {

        public void handle(String pathInContext, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            int load = 100;
            httpResponse.setStatus(200);
            httpResponse.setContentLength(load);
            httpResponse.setContentType("application/octet-stream");

            httpResponse.flushBuffer();

            OutputStream os = httpResponse.getOutputStream();
            for (int i = 0; i < load; i++) {
                os.write(i % 255);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    // nuku
                }

                if (i > load / 10) {
                    httpResponse.sendError(500);
                }
            }

            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }

    protected String getTargetUrl() {
        return String.format("http://127.0.0.1:%d/", port1);
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new SlowAndBigHandler();
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testMaxRetry() throws Exception {
        AsyncHttpClient ahc = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setMaxRequestRetry(0).build());
        try {
            ahc.executeRequest(ahc.prepareGet(getTargetUrl()).build()).get();
            fail();
        } catch (Exception t) {
            assertNotNull(t.getCause());
            assertEquals(t.getCause().getClass(), IOException.class);
            if (!t.getCause().getMessage().startsWith("Remotely Closed")) {
                fail();
            }
        } finally {
            ahc.close();
        }
    }
}
