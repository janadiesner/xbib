
package org.xbib.template.handlebars;

import java.io.IOException;

/**
 * Handlebars helpers can be accessed from any context in a template. You can
 * register a helper with the {@link Handlebars#registerHelper(String, Helper)}
 * method.
 *
 * @param <T> The context object.
 */
public interface Helper<T> {

    /**
     * Apply the helper to the context.
     *
     * @param context The context object.
     * @param options The options object.
     * @return A string result.
     * @throws java.io.IOException If a template cannot be loaded.
     */
    CharSequence apply(T context, Options options) throws IOException;
}
