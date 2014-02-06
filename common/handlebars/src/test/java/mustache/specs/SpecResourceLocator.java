
package mustache.specs;

import org.xbib.template.handlebars.io.StringTemplateSource;
import org.xbib.template.handlebars.io.TemplateSource;
import org.xbib.template.handlebars.io.URLTemplateLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.xbib.template.handlebars.util.Validate.notEmpty;
import static org.xbib.template.handlebars.util.Validate.notNull;

public class SpecResourceLocator extends URLTemplateLoader {
    private Map<String, String> templates;

    public SpecResourceLocator(final Spec spec) {
        templates = spec.partials();
        if (templates == null) {
            templates = new HashMap<String, String>();
        }
        templates.put("template", spec.template());
    }

    @Override
    public TemplateSource sourceAt(final String uri) throws IOException {
        notNull(uri, "The uri is required.");
        notEmpty(uri.toString(), "The uri is required.");
        String location = resolve(normalize(uri));
        String text = templates.get(uri.toString());
        if (text == null) {
            throw new FileNotFoundException(location);
        }
        return new StringTemplateSource(location, text);
    }

    @Override
    protected URL getResource(final String location) throws IOException {
        throw new UnsupportedOperationException();
    }
}
