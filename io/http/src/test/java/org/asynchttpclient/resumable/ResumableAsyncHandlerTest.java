package org.asynchttpclient.resumable;

import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ResumableAsyncHandlerTest {
    @Test
    public void testAdjustRange() {
        MapResumableProcessor proc = new MapResumableProcessor();

        ResumableAsyncHandler h = new ResumableAsyncHandler(proc);
        Request request = new RequestBuilder("GET").setUrl("http://test/url").build();
        Request newRequest = h.adjustRequestRange(request);
        assertEquals(newRequest.getUrl(), request.getUrl());
        String rangeHeader = newRequest.getHeaders().getFirstValue("Range");
        assertNull(rangeHeader);

        proc.put("http://test/url", 5000);
        newRequest = h.adjustRequestRange(request);
        assertEquals(newRequest.getUrl(), request.getUrl());
        rangeHeader = newRequest.getHeaders().getFirstValue("Range");
        assertEquals(rangeHeader, "bytes=5000-");
    }
}
