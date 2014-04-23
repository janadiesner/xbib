
package org.xbib.template.handlebars.cache;

import org.xbib.template.handlebars.Parser;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.io.TemplateSource;

import java.io.IOException;

/**
 * The template cache system.
 */
public interface TemplateCache {

    /**
     * Remove all mappings from the cache.
     */
    void clear();

    /**
     * Evict the mapping for this source from this cache if it is present.
     *
     * @param source the source whose mapping is to be removed from the cache
     */
    void evict(TemplateSource source);

    /**
     * Return the value to which this cache maps the specified key.
     *
     * @param source source whose associated template is to be returned.
     * @param parser The Handlebars parser.
     * @return A template.
     * @throws java.io.IOException If input can't be parsed.
     */
    Template get(TemplateSource source, Parser parser) throws IOException;
}
