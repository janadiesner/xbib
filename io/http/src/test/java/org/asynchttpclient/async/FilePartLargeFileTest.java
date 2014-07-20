package org.asynchttpclient.async;

import static org.asynchttpclient.async.util.TestUtils.*;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.FilePart;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

public abstract class FilePartLargeFileTest extends AbstractBasicTest {

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new AbstractHandler() {

            public void handle(String arg0, Request arg1, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

                ServletInputStream in = req.getInputStream();
                byte[] b = new byte[8192];

                int count = -1;
                int total = 0;
                while ((count = in.read(b)) != -1) {
                    b = new byte[8192];
                    total += count;
                }
                resp.setStatus(200);
                resp.addHeader("X-TRANFERED", String.valueOf(total));
                resp.getOutputStream().flush();
                resp.getOutputStream().close();

                arg1.setHandled(true);
            }
        };
    }

    @Test(groups = { "standalone", "default_provider" }, enabled = true)
    public void testPutImageFile() throws Exception {
        AsyncHttpClient client = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeoutInMs(100 * 6000).build());
        try {
            Response response = client.preparePut(getTargetUrl()).addBodyPart(new FilePart("test", LARGE_IMAGE_FILE, "application/octet-stream", "UTF-8")).execute().get();
            assertEquals(response.getStatusCode(), 200);
        } finally {
            client.close();
        }
    }

    @Test(groups = { "standalone", "default_provider" }, enabled = true)
    public void testPutLargeTextFile() throws Exception {
        File file = createTempFile(1024 * 1024);

        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            Response response = client.preparePut(getTargetUrl()).addBodyPart(new FilePart("test", file, "application/octet-stream", "UTF-8")).execute().get();
            assertEquals(response.getStatusCode(), 200);
        } finally {
            client.close();
        }
    }
}
