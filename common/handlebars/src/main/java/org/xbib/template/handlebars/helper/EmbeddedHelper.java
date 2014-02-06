
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;
import org.xbib.template.handlebars.Template;

import java.io.IOException;

import static org.xbib.template.handlebars.util.Validate.notEmpty;

/**
 * Given:
 * home.hbs
 * <p/>
 * <pre>
 * &lt;html&gt;
 * ...
 * {{emdedded "user" ["id"]}}
 * &lt;/html&gt;
 * </pre>
 * <p/>
 * where user.hbs is:
 * <p/>
 * <pre>
 * &lt;tr&gt;
 * &lt;td&gt;{{firstName}}&lt;/td&gt;
 * &lt;td&gt;{{lastName}}&lt;/td&gt;
 * &lt;/tr&gt;
 * </pre>
 * <p/>
 * expected output is:
 * <p/>
 * <pre>
 * &lt;script id="user-hbs" type="text/x-handlebars-template"&gt;
 * &lt;tr&gt;
 * &lt;td&gt;{{firstName}}&lt;/td&gt;
 * &lt;td&gt;{{lastName}}&lt;/td&gt;
 * &lt;/tr&gt;
 * &lt;/script&gt;
 * </pre>
 * <p/>
 * Optionally, a user can set the template's name:
 * <p/>
 * <pre>
 * {{emdedded "user" "user-tmpl" }}
 * </pre>
 * <p/>
 * expected output is:
 * <p/>
 * <pre>
 * &lt;script id="user-tmpl" type="text/x-handlebars-template"&gt;
 * &lt;tr&gt;
 * &lt;td&gt;{{firstName}}&lt;/td&gt;
 * &lt;td&gt;{{lastName}}&lt;/td&gt;
 * &lt;/tr&gt;
 * &lt;/script&gt;
 * </pre>
 */
public class EmbeddedHelper implements Helper<String> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<String> INSTANCE = new EmbeddedHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "embedded";

    @Override
    public CharSequence apply(final String path, final Options options)
            throws IOException {
        notEmpty(path, "found '%s', expected 'partial's name'", path);
        String suffix = options.handlebars.getLoader().getSuffix();
        String defaultId = (path + suffix).replace('/', '-').replace('.', '-');
        String id = options.param(0, defaultId);
        Template template = options.handlebars.compile(path);
        StringBuilder script = new StringBuilder();
        script.append("<script id=\"").append(id)
                .append("\" type=\"text/x-handlebars\">\n");
        script.append(template.text()).append("\n");
        script.append("</script>");
        return new Handlebars.SafeString(script);
    }
}
