package org.asynchttpclient.async;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests to reproduce issues with handling of error responses
 * 
 */
public abstract class ErrorResponseTest extends AbstractBasicTest {
    final static String BAD_REQUEST_STR = "Very Bad Request! No cookies.";

    private static class ErrorHandler extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            try {
                Thread.sleep(210L);
            } catch (InterruptedException e) {
            }
            response.setContentType("text/plain");
            response.setStatus(400);
            OutputStream out = response.getOutputStream();
            out.write(BAD_REQUEST_STR.getBytes("UTF-8"));
            out.flush();
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new ErrorHandler();
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testQueryParameters() throws Exception {
        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            Future<Response> f = client.prepareGet("http://127.0.0.1:" + port1 + "/foo").addHeader("Accepts", "*/*").execute();
            Response resp = f.get(3, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), 400);
            assertEquals(resp.getResponseBody(), BAD_REQUEST_STR);
        } finally {
            client.close();
        }
    }
}
