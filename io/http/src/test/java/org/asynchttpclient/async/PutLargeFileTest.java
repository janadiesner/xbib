package org.asynchttpclient.async;

import static org.asynchttpclient.async.util.TestUtils.createTempFile;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

public abstract class PutLargeFileTest extends AbstractBasicTest {

    @Test(groups = { "standalone", "default_provider" }, enabled = true)
    public void testPutLargeFile() throws Exception {

        File file = createTempFile(1024 * 1024);

        int timeout = (int) file.length() / 1000;

        AsyncHttpClient client = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setConnectionTimeoutInMs(timeout).build());
        try {
            Response response = client.preparePut(getTargetUrl()).setBody(file).execute().get();
            assertEquals(response.getStatusCode(), 200);
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testPutSmallFile() throws Exception {

        File file = createTempFile(1024);

        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            Response response = client.preparePut(getTargetUrl()).setBody(file).execute().get();
            assertEquals(response.getStatusCode(), 200);
        } finally {
            client.close();
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new AbstractHandler() {

            public void handle(String arg0, Request arg1, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

                resp.setStatus(200);
                resp.getOutputStream().flush();
                resp.getOutputStream().close();

                arg1.setHandled(true);
            }
        };
    }
}
