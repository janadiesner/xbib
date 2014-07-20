package org.asynchttpclient.async;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.FluentCaseInsensitiveStringsMap;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public abstract class InputStreamTest extends AbstractBasicTest {

    private static class InputStreamHandler extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                byte[] bytes = new byte[3];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while (read > -1) {
                    read = request.getInputStream().read(bytes);
                    if (read > 0) {
                        bos.write(bytes, 0, read);
                    }
                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.addHeader("X-Param", new String(bos.toByteArray()));
            } else { // this handler is to handle POST request
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new InputStreamHandler();
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testInvalidInputStream() throws IOException, ExecutionException, TimeoutException, InterruptedException {

        AsyncHttpClient c = getAsyncHttpClient(null);
        try {
            FluentCaseInsensitiveStringsMap h = new FluentCaseInsensitiveStringsMap();
            h.add("Content-Type", "application/x-www-form-urlencoded");

            InputStream is = new InputStream() {

                public int readAllowed;

                @Override
                public int available() {
                    return 1; // Fake
                }

                @Override
                public int read() throws IOException {
                    int fakeCount = readAllowed++;
                    if (fakeCount == 0) {
                        return (int) 'a';
                    } else if (fakeCount == 1) {
                        return (int) 'b';
                    } else if (fakeCount == 2) {
                        return (int) 'c';
                    } else {
                        return -1;
                    }
                }
            };

            Response resp = c.preparePost(getTargetUrl()).setHeaders(h).setBody(is).execute().get();
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("X-Param"), "abc");
        } finally {
            c.close();
        }
    }
}
