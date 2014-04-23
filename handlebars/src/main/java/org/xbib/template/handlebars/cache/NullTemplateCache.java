
package org.xbib.template.handlebars.cache;

import org.xbib.template.handlebars.Parser;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.io.TemplateSource;

import java.io.IOException;

/**
 * Null cache implementation.
 */
public enum NullTemplateCache implements TemplateCache {

    /**
     * Shared instance of null cache.
     */
    INSTANCE;

    @Override
    public void clear() {
    }

    @Override
    public void evict(final TemplateSource source) {
    }

    @Override
    public Template get(final TemplateSource source, final Parser parser) throws IOException {
        return parser.parse(source);
    }

}
