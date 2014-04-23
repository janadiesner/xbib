
package org.xbib.web;

import org.xbib.io.negotiate.ContentTypeNegotiator;

import java.util.regex.Pattern;

public class BrowserContentTypeNegotiator extends ContentTypeNegotiator {

    public BrowserContentTypeNegotiator() {
        super();
        setDefaultAccept("text/html");
        /*
         * Send HTML to clients that indicate they accept everything.
         * This is specifically so that cURL sees HTML, and also catches
         * various browsers that send "* / *" in some circumstances.
         */
        addUserAgentOverride(null, "*/*", "text/html");

        /**
         * MSIE (7.0) sends either \* / *, or * / * with a list of other
         * random types,
         * but always without q values. That's useless. We will simply send
         * HTML to MSIE, no matter what. Boy, do I hate IE.
         */
        addUserAgentOverride(Pattern.compile("MSIE"), null, "text/html");

        addVariant("text/xml;q=0.81").addAliasMediaType("text/xml;q=0.81");

        // for browser "Accept"
        addVariant("application/json;q=0.80").addAliasMediaType("application/json;q=0.80");

    }
}
