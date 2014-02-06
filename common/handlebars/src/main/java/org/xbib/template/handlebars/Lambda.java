
package org.xbib.template.handlebars;

import java.io.IOException;

/**
 * <p>
 * When the value is a callable object, such as a lambda, the object will be
 * invoked and passed the block of text. The text passed is the literal block,
 * unrendered. {{tags}} will not have been expanded - the lambda should do that
 * on its own. In this way you can implement filters or caching.
 * </p>
 * <p>
 * Template:
 * </p>
 * <p/>
 * <pre>
 * {{#wrapped}}
 * {{name}} is awesome.
 * {{/wrapped}}
 * </pre>
 * <p/>
 * Hash:
 * <p/>
 * <pre>
 * Map hash = ...
 * hash.put("name", "Willy");
 * hash.put("wrapped", new Lambda<String>() {
 *   public String apply(Scope scope, Template template) {
 *    return "<b>" + template.apply(scope) + "</b>";
 *   }
 * });
 * </pre>
 * <p>
 * Output:
 * </p>
 * <p/>
 * <pre>
 * <b>Willy is awesome.</b>
 * </pre>
 *
 * @param <C> The lambda context.
 * @param <O> The lambda output.
 */
public interface Lambda<C, O> {

    /**
     * Apply the lambda.
     *
     * @param context  The current context.
     * @param template The current template.
     * @return The resulting text.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    O apply(C context, Template template) throws IOException;
}
