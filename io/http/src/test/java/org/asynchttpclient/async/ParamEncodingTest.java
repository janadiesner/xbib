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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.asynchttpclient.util.MiscUtil.isNonEmpty;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public abstract class ParamEncodingTest extends AbstractBasicTest {

    private class ParamEncoding extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                String p = request.getParameter("test");
                if (isNonEmpty(p)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.addHeader("X-Param", p);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                }
            } else { // this handler is to handle POST request
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testParameters() throws IOException, ExecutionException, TimeoutException, InterruptedException {

        String value = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKQLMNOPQRSTUVWXYZ1234567809`~!@#$%^&*()_+-=,.<>/?;:'\"[]{}\\| ";
        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            Future<Response> f = client.preparePost("http://127.0.0.1:" + port1).addParameter("test", value).execute();
            Response resp = f.get(10, TimeUnit.SECONDS);
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("X-Param"), value.trim());
        } finally {
            client.close();
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new ParamEncoding();
    }
}
