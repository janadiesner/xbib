package org.asynchttpclient.providers.netty;

import static org.testng.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.asynchttpclient.Cookie;
import org.asynchttpclient.FluentCaseInsensitiveStringsMap;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.providers.netty.response.NettyResponse;
import org.asynchttpclient.providers.netty.response.ResponseStatus;
import org.testng.annotations.Test;

public class NettyAsyncResponseTest {

    @Test(groups = "standalone")
    public void testCookieParseExpires() {
        // e.g. "Sun, 06-Feb-2012 03:45:24 GMT";
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date date = new Date(System.currentTimeMillis() + 60000); // sdf.parse( dateString );
        final String cookieDef = String.format("efmembercheck=true; expires=%s; path=/; domain=.eclipse.org", sdf.format(date));

        NettyResponse
                response = new NettyResponse(new ResponseStatus(null, null, null), new HttpResponseHeaders() {
            @Override
            public FluentCaseInsensitiveStringsMap getHeaders() {
                return new FluentCaseInsensitiveStringsMap().add("Set-Cookie", cookieDef);
            }
        }, null);

        List<Cookie> cookies = response.getCookies();
        assertEquals(cookies.size(), 1);

        Cookie cookie = cookies.get(0);
        assertTrue(cookie.getMaxAge() > 55 && cookie.getMaxAge() < 61, "");
    }

    @Test(groups = "standalone")
    public void testCookieParseMaxAge() {
        final String cookieDef = "efmembercheck=true; max-age=60; path=/; domain=.eclipse.org";
        NettyResponse response = new NettyResponse(new ResponseStatus(null, null, null), new HttpResponseHeaders() {
            @Override
            public FluentCaseInsensitiveStringsMap getHeaders() {
                return new FluentCaseInsensitiveStringsMap().add("Set-Cookie", cookieDef);
            }
        }, null);
        List<Cookie> cookies = response.getCookies();
        assertEquals(cookies.size(), 1);

        Cookie cookie = cookies.get(0);
        assertEquals(cookie.getMaxAge(), 60);
    }

    @Test(groups = "standalone")
    public void testCookieParseWeirdExpiresValue() {
        final String cookieDef = "efmembercheck=true; expires=60; path=/; domain=.eclipse.org";
        NettyResponse response = new NettyResponse(new ResponseStatus(null, null, null), new HttpResponseHeaders() {
            @Override
            public FluentCaseInsensitiveStringsMap getHeaders() {
                return new FluentCaseInsensitiveStringsMap().add("Set-Cookie", cookieDef);
            }
        }, null);

        List<Cookie> cookies = response.getCookies();
        assertEquals(cookies.size(), 1);

        Cookie cookie = cookies.get(0);
        assertEquals(cookie.getMaxAge(), 60);
    }

}
