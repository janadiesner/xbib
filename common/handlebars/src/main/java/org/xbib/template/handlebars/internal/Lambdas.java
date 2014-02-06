
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.Context;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Lambda;
import org.xbib.template.handlebars.TagType;
import org.xbib.template.handlebars.Template;

import java.io.IOException;

/**
 * Utilities function for work with lambdas.
 */
final class Lambdas {

    /**
     * Not allowed.
     */
    private Lambdas() {
    }

    /**
     * Merge the lambda result.
     *
     * @param handlebars The handlebars.
     * @param lambda     The lambda.
     * @param scope      The current scope.
     * @param template   The current template.
     * @return The resulting text.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public static CharSequence merge(final Handlebars handlebars,
                                     final Lambda<Object, Object> lambda, final Context scope,
                                     final Template template) throws IOException {
        Template result = compile(handlebars, lambda, scope, template);
        return result.apply(scope);
    }

    /**
     * Compile the given lambda.
     *
     * @param handlebars The handlebars.
     * @param lambda     The lambda.
     * @param scope      The current scope.
     * @param template   The template.
     * @return The resulting template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public static Template compile(final Handlebars handlebars,
                                   final Lambda<Object, Object> lambda, final Context scope,
                                   final Template template)
            throws IOException {
        return compile(handlebars, lambda, scope, template, "{{", "}}");
    }

    /**
     * Compile the given lambda.
     *
     * @param handlebars     The handlebars.
     * @param lambda         The lambda.
     * @param scope          The current scope.
     * @param template       The template.
     * @param startDelimiter The start delimiter.
     * @param endDelimiter   The end delimiter.
     * @return The resulting template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public static Template compile(final Handlebars handlebars,
                                   final Lambda<Object, Object> lambda, final Context scope,
                                   final Template template, final String startDelimiter,
                                   final String endDelimiter)
            throws IOException {
        Object value = lambda.apply(scope, template);
        final Template result;
        if (value instanceof CharSequence) {
            result = handlebars.compileInline(value.toString(), startDelimiter, endDelimiter);
        } else {
            // Don't escape no string values.
            result = new Variable(handlebars, "$$lambda", value, TagType.TRIPLE_VAR);
        }
        return result;
    }
}
