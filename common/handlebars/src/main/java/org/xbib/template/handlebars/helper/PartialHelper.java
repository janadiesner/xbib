
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;

import static org.xbib.template.handlebars.util.Validate.isTrue;

/**
 * The partial registry helper. It stores templates in the current execution
 * context. Later the BLOCK helper read the registry and apply the
 * template.
 */
public class PartialHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new PartialHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "partial";

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        isTrue(context instanceof String, "found '%s', expected 'partial's name'",
                context);

        options.partial((String) context, options.fn);
        return null;
    }
}
