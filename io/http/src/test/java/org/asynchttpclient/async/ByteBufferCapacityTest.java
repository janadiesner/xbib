
package org.asynchttpclient.async;

import static org.asynchttpclient.async.util.TestUtils.createTempFile;
import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

public abstract class ByteBufferCapacityTest extends AbstractBasicTest {

    private class BasicHandler extends AbstractHandler {

        public void handle(String s, Request r, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            Enumeration<?> e = httpRequest.getHeaderNames();
            String param;
            while (e.hasMoreElements()) {
                param = e.nextElement().toString();
                httpResponse.addHeader("X-" + param, httpRequest.getHeader(param));
            }

            int size = 10 * 1024;
            if (httpRequest.getContentLength() > 0) {
                size = httpRequest.getContentLength();
            }
            byte[] bytes = new byte[size];
            if (bytes.length > 0) {
                final InputStream in = httpRequest.getInputStream();
                final OutputStream out = httpResponse.getOutputStream();
                int read;
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
            }

            httpResponse.setStatus(200);
            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new BasicHandler();
    }

    @Test(groups = { "standalone", "default_provider" })
    public void basicByteBufferTest() throws Exception {
        AsyncHttpClient c = getAsyncHttpClient(null);
        try {
            File largeFile = createTempFile(1024 * 100 * 10);
            final AtomicInteger byteReceived = new AtomicInteger();

            try {
                Response response = c.preparePut(getTargetUrl()).setBody(largeFile).execute(new AsyncCompletionHandlerAdapter() {
                    @Override
                    public STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
                        byteReceived.addAndGet(content.getBodyByteBuffer().capacity());
                        return super.onBodyPartReceived(content);
                    }

                }).get();

                assertNotNull(response);
                assertEquals(response.getStatusCode(), 200);
                assertEquals(byteReceived.get(), largeFile.length());
                assertEquals(response.getResponseBody().length(), largeFile.length());

            } catch (IOException ex) {
                fail("Should have timed out");
            }
        } finally {
            c.close();
        }
    }

    public String getTargetUrl() {
        return String.format("http://127.0.0.1:%d/foo/test", port1);
    }
}
