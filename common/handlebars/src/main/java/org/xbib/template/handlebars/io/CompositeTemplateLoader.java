
package org.xbib.template.handlebars.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.xbib.template.handlebars.util.Validate.isTrue;

/**
 * <p>
 * Combine two or more {@link org.xbib.template.handlebars.io.TemplateLoader} as a single {@link org.xbib.template.handlebars.io.TemplateLoader}.
 * {@link org.xbib.template.handlebars.io.TemplateLoader}s are executed in the order they are provided.
 * </p>
 * <p>
 * Execution is as follows:
 * </p>
 * <ul>
 * <li>If a {@link org.xbib.template.handlebars.io.TemplateLoader} is able to resolve a {@link org.xbib.template.handlebars.io.TemplateSource}, that
 * {@link org.xbib.template.handlebars.io.TemplateSource} is considered the response.</li>
 * <li>If a {@link org.xbib.template.handlebars.io.TemplateLoader} throws a {@link java.io.IOException} exception the next
 * {@link org.xbib.template.handlebars.io.TemplateLoader} in the chain will be used.</li>
 * </ul>
 */
public class CompositeTemplateLoader implements TemplateLoader {

    /**
     * The template loader list.
     */
    private final TemplateLoader[] delegates;

    /**
     * Creates a new {@link org.xbib.template.handlebars.io.CompositeTemplateLoader}.
     *
     * @param loaders The template loader chain. At least two loaders must be provided.
     */
    public CompositeTemplateLoader(final TemplateLoader... loaders) {
        isTrue(loaders.length > 1, "At least two loaders are required.");
        this.delegates = loaders;
    }

    @Override
    public TemplateSource sourceAt(final String location) throws IOException {
        for (TemplateLoader delegate : delegates) {
            try {
                return delegate.sourceAt(location);
            } catch (IOException ex) {
                // try next loader in the chain.
            }
        }
        throw new FileNotFoundException(location);
    }

    @Override
    public String resolve(final String location) {
        for (TemplateLoader delegate : delegates) {
            try {
                delegate.sourceAt(location);
                return delegate.resolve(location);
            } catch (IOException ex) {
                // try next loader in the chain.
            }
        }
        throw new IllegalStateException("Can't resolve: '" + location + "'");
    }

    @Override
    public String getPrefix() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSuffix() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the delegates template loaders.
     *
     * @return The delegates template loaders.
     */
    public Iterable<TemplateLoader> getDelegates() {
        return Arrays.asList(delegates);
    }
}
