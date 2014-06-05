package org.xbib.web.dispatcher;

import org.xbib.io.negotiate.ContentTypeNegotiator;
import org.xbib.io.negotiate.MediaRangeSpec;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@ApplicationPath("app")
public class DispatcherApplication extends Application {

    private final static Logger logger = LoggerFactory.getLogger(DispatcherApplication.class.getName());

    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(
                TestService.class,
                RoutingService.class,
                DemoService.class
        ));
    }

    public static String negotiateMediaType(String useragent, String accept) {
        String mediaType = mediaTypes.get(accept);
        if (mediaType == null) {
            MediaRangeSpec mrs = useragent != null ?
                    ctn.getBestMatch(accept, useragent) :
                    ctn.getBestMatch(accept);
            mediaType = mrs != null ? mrs.getMediaType() : "";
            mediaTypes.put(accept, mediaType);
        }
        logger.info("content negotiate: useragent = {}, accept = {} --> mediaType = {}",
                useragent, accept, mediaType);
        return mediaType;
    }

    private final static Map<String, String> mediaTypes = new HashMap<>();

    private final static ContentTypeNegotiator ctn = new BrowserContentTypeNegotiator();

    private static class BrowserContentTypeNegotiator extends ContentTypeNegotiator {

        public BrowserContentTypeNegotiator() {
            super();
            setDefaultAccept("text/html");
            addUserAgentOverride(null, "*/*", "text/html");
            addUserAgentOverride(Pattern.compile("MSIE"), null, "text/html");
            addVariant("text/xml;q=0.81").addAliasMediaType("text/xml;q=0.81");
            addVariant("application/json;q=0.80").addAliasMediaType("application/json;q=0.80");

        }
    }
}
