
package org.xbib.template.handlebars.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static org.xbib.template.handlebars.util.Validate.notEmpty;

/**
 * <p>
 * Strategy interface for loading resources (i.e class path or file system resources)
 * </p>
 * <h3>Templates prefix and suffix</h3>
 * <p>
 * A <code>TemplateLoader</code> provides two important properties:
 * </p>
 * <ul>
 * <li>prefix: useful for setting a default prefix where templates are stored.</li>
 * <li>suffix: useful for setting a default suffix or file extension for your templates. Default is:
 * <code>'.hbs'</code></li>
 * </ul>
 * <p>
 * Usage:
 * </p>
 * <p/>
 * <pre>
 * TemplateLoader loader = new ClassPathTemplateLoader();
 * loader.setPrefix("/templates");
 * loader.setSuffix(".html");
 * Handlebars handlebars = new Handlebars(loader);
 *
 * Template template = handlebars.compile("mytemplate");
 *
 * String s = template.apply("Handlebars.java");
 * </pre>
 * <p/>
 * <p>
 * The template loader resolve <code>mytemplate</code> to <code>/templates/mytemplate.html</code>
 * and load it.
 * </p>
 */
public abstract class URLTemplateLoader extends AbstractTemplateLoader {

    @Override
    public TemplateSource sourceAt(final String uri) throws IOException {
        notEmpty(uri, "The uri is required.");
        String location = resolve(normalize(uri));
        URL resource = getResource(location);
        if (resource == null) {
            throw new FileNotFoundException(location);
        }
        return new URLTemplateSource(location, resource);
    }

    /**
     * Get a template resource for the given location.
     *
     * @param location The location of the template source. Required.
     * @return A new template resource.
     * @throws java.io.IOException If the url can't be resolved.
     */
    protected abstract URL getResource(String location) throws IOException;

}
