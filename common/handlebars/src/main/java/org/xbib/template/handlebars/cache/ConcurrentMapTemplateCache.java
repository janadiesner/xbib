
package org.xbib.template.handlebars.cache;

import org.xbib.template.handlebars.util.Pair;
import org.xbib.template.handlebars.Parser;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.io.ForwardingTemplateSource;
import org.xbib.template.handlebars.io.TemplateSource;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * A simple {@link org.xbib.template.handlebars.cache.TemplateCache} built on top of {@link java.util.concurrent.ConcurrentHashMap}.
 */
public class ConcurrentMapTemplateCache implements TemplateCache {

    /**
     * The map cache.
     */
    private final ConcurrentMap<TemplateSource, Pair<TemplateSource, Template>> cache;

    /**
     * Creates a new ConcurrentMapTemplateCache.
     *
     * @param cache The concurrent map cache. Required.
     */
    protected ConcurrentMapTemplateCache(
            final ConcurrentMap<TemplateSource, Pair<TemplateSource, Template>> cache) {
        this.cache = notNull(cache, "The cache is required.");
    }

    /**
     * Creates a new ConcurrentMapTemplateCache.
     */
    public ConcurrentMapTemplateCache() {
        this(new ConcurrentHashMap<TemplateSource, Pair<TemplateSource, Template>>());
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void evict(final TemplateSource source) {
        cache.remove(source);
    }

    @Override
    public Template get(final TemplateSource source, final Parser parser) throws IOException {
        notNull(source, "The source is required.");
        notNull(parser, "The parser is required.");

        /**
         * Don't keep duplicated entries, remove old one if a change is detected.
         */
        return cacheGet(new ForwardingTemplateSource(source) {
            @Override
            public boolean equals(final Object obj) {
                if (obj instanceof TemplateSource) {
                    return source.filename().equals(((TemplateSource) obj).filename());
                }
                return false;
            }

            @Override
            public int hashCode() {
                return source.filename().hashCode();
            }
        }, parser);
    }

    /**
     * Get/Parse a template source.
     *
     * @param source The template source.
     * @param parser The parser.
     * @return A Handlebars template.
     * @throws java.io.IOException If we can't read input.
     */
    private Template cacheGet(final TemplateSource source, final Parser parser) throws IOException {
        Pair<TemplateSource, Template> entry = cache.get(source);
        if (entry == null) {
            entry = Pair.of(source, parser.parse(source));
            cache.put(source, entry);
        } else if (source.lastModified() != entry.getLeft().lastModified()) {
            // remove current entry.
            evict(source);
            entry = Pair.of(source, parser.parse(source));
            cache.put(source, entry);
        }
        return entry.getRight();
    }

}
