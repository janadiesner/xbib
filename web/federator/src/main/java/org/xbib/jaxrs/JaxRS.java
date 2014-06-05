
package org.xbib.jaxrs;

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

@ApplicationPath("/")
public class JaxRS extends Application {

    private final static Logger logger = LoggerFactory.getLogger(JaxRS.class.getName());

    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(SearchService.class, FederatorService.class));
    }

    public final static Map<String,String> mimeTypeFilters = new HashMap<String,String>() {{
        put("text/html","xsl/es-searchretrieve-response.xsl,xsl/es-searchretrieve-lia-html.xsl"); // MODS HTML
        put("text/xml", "xsl/es-mods-xml.xsl");  // MODS XML
        put("application/xml", "xsl/identity.xsl"); // XML
        put("application/mods+html", "xsl/es-searchretrieve-response.xsl,xsl/es-searchretrieve-lia-html.xsl");
        put("application/mods+xml", "xsl/es-mods-xml.xsl");
        put("application/sru+xml", "xsl/es-searchretrieve-response.xsl"); // SRU
    }};

    private final static Map<String, String> mediaTypes = new HashMap();

    private final static ContentTypeNegotiator ctn = new ContentTypeNegotiator();

    public static String negotiateMediaType(String useragent, String accept) {
        String mediaType = mediaTypes.get(accept);
        if (mediaType == null) {
            MediaRangeSpec mrs = useragent != null ?
                    ctn.getBestMatch(accept, useragent) :
                    ctn.getBestMatch(accept);
            mediaType = mrs != null ? mrs.getMediaType() : "";
            mediaTypes.put(accept, mediaType);
        }
        logger.debug("content negotiate: useragent = {}, accept = {} --> mediaType = {}",
                useragent, accept, mediaType);
        return mediaType;
    }

}
