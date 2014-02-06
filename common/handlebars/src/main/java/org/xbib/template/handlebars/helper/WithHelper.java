
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;

/**
 * <p>
 * Normally, Handlebars templates are evaluated against the context passed into
 * the compiled method.
 * </p>
 * <p>
 * You can shift the context for a section of a template by using the built-in
 * with block helper.
 * </p>
 */
public class WithHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new WithHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "with";

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        if (options.isFalsy(context)) {
            return options.inverse(context);
        } else {
            return options.fn(context);
        }
    }
}
