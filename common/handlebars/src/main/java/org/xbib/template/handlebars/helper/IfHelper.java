
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;

/**
 * You can use the if helper to conditionally render a block. If its argument
 * returns false, null or empty list/array (a "falsy" value), Handlebars will
 * not render the block.
 */
public class IfHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new IfHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "if";

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        if (options.isFalsy(context)) {
            return options.inverse();
        } else {
            return options.fn();
        }
    }
}
