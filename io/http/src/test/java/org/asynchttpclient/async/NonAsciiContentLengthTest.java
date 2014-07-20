package org.asynchttpclient.async;

import static org.asynchttpclient.async.util.TestUtils.*;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class NonAsciiContentLengthTest extends AbstractBasicTest {

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        server = newJettyHttpServer(port1);
        server.setHandler(new AbstractHandler() {

            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                int MAX_BODY_SIZE = 1024; // Can only handle bodies of up to 1024 bytes.
                byte[] b = new byte[MAX_BODY_SIZE];
                int offset = 0;
                int numBytesRead;
                ServletInputStream is = request.getInputStream();
                try {
                    while ((numBytesRead = is.read(b, offset, MAX_BODY_SIZE - offset)) != -1) {
                        offset += numBytesRead;
                    }
                } finally {
                    is.close();
                }
                assertEquals(request.getContentLength(), offset);
                response.setStatus(200);
                response.setCharacterEncoding(request.getCharacterEncoding());
                response.setContentLength(request.getContentLength());
                ServletOutputStream os = response.getOutputStream();
                try {
                    os.write(b, 0, offset);
                } finally {
                    os.close();
                }
            }
        });
        server.start();
    }

    @Test(groups = { "standalone", "default_provider" })
    public void testNonAsciiContentLength() throws Exception {
        execute("test");
        execute("\u4E00"); // Unicode CJK ideograph for one
    }

    protected void execute(String body) throws IOException, InterruptedException, ExecutionException {
        AsyncHttpClient client = getAsyncHttpClient(null);
        try {
            BoundRequestBuilder r = client.preparePost(getTargetUrl()).setBody(body).setBodyEncoding("UTF-8");
            Future<Response> f = r.execute();
            Response resp = f.get();
            assertEquals(resp.getStatusCode(), 200);
            assertEquals(body, resp.getResponseBody("UTF-8"));
        } finally {
            client.close();
        }
    }
}
