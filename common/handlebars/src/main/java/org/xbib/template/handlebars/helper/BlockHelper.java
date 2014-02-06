
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;
import org.xbib.template.handlebars.Template;

import java.io.IOException;

import static org.xbib.template.handlebars.util.Validate.isTrue;

/**
 * <p>
 * The block helper will replace its section with the partial of the same name if it exists.
 * </p>
 * <p>
 * If <code>delete-after-merge</code> is set to <code>true</code>, the partial will be delete once
 * applied it.
 * </p>
 * <p/>
 * <pre>
 *  {{block "title" [delete-after-merge=false]}}
 * </pre>
 */
public class BlockHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new BlockHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "block";

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        isTrue(context instanceof String, "found '%s', expected 'partial's name'",
                context);

        String path = (String) context;
        Template template = options.partial(path);
        if (template == null) {
            try {
                template = options.handlebars.compile(path);
            } catch (IOException ex) {
                // assume partial not found
                template = options.fn;
            }
        }
        CharSequence result = options.apply(template);
        Boolean deletePartials = options.hash("delete-after-merge", false);
        if (deletePartials) {
            // once applied, remove the template from current execution.
            options.partial(path, null);
        }
        return result;
    }
}
