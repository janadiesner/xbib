
package org.xbib.template.handlebars;

import org.xbib.template.handlebars.io.StringTemplateSource;
import org.xbib.template.handlebars.io.TemplateSource;
import org.xbib.template.handlebars.io.URLTemplateLoader;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.xbib.template.handlebars.util.Validate.notEmpty;
import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * Template loader for testing.
 */
public class MapTemplateLoader extends URLTemplateLoader {

    private Map<String, String> map;

    public MapTemplateLoader(final Map<String, String> map) {
        this.map = map;
    }

    public MapTemplateLoader() {
        this(new HashMap<String, String>());
    }

    public MapTemplateLoader define(final String name, final String content) {
        map.put(getPrefix() + name + getSuffix(), content);
        return this;
    }

    @Override
    public TemplateSource sourceAt(final String uri) throws FileNotFoundException {
        notNull(uri, "The uri is required.");
        notEmpty(uri, "The uri is required.");
        String location = resolve(normalize(uri));
        String text = map.get(location);
        if (text == null) {
            throw new FileNotFoundException(location);
        }
        return new StringTemplateSource(location, text);
    }

    @Override
    protected URL getResource(final String location) {
        throw new UnsupportedOperationException();
    }

}
