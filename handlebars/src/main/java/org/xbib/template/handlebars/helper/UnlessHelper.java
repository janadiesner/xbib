
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;

/**
 * You can use the unless helper as the inverse of the if helper. Its block
 * will be rendered if the expression returns a falsy value.
 */
public class UnlessHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new UnlessHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "unless";

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        if (options.isFalsy(context)) {
            return options.fn();
        } else {
            return options.inverse();
        }
    }
}
