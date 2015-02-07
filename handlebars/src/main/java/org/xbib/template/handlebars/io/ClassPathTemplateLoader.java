
package org.xbib.template.handlebars.io;

import java.net.URL;


/**
 * Load templates from the class-path. A base path can be specified at creation
 * time. By default all the templates are loaded from '/'.
 */
public class ClassPathTemplateLoader extends URLTemplateLoader {

    private final ClassLoader classLoader;

    public ClassPathTemplateLoader() {
        this(ClassPathTemplateLoader.class.getClassLoader(), "/", DEFAULT_SUFFIX);
    }

    /**
     * Creates a new {@link org.xbib.template.handlebars.io.ClassPathTemplateLoader}.
     *
     * @param prefix The view prefix. Required.
     */
    public ClassPathTemplateLoader(final String prefix) {
        this(ClassPathTemplateLoader.class.getClassLoader(), prefix, DEFAULT_SUFFIX);
    }

    /**
     * Creates a new {@link org.xbib.template.handlebars.io.ClassPathTemplateLoader}.
     *
     * @param prefix The view prefix. Required.
     * @param suffix The view suffix. Required.
     */
    public ClassPathTemplateLoader(final String prefix, final String suffix) {
        this(ClassPathTemplateLoader.class.getClassLoader(), prefix, suffix);
    }

    public ClassPathTemplateLoader(ClassLoader classLoader) {
        this(classLoader, "/", DEFAULT_SUFFIX);
    }

    public ClassPathTemplateLoader(ClassLoader classLoader, final String prefix) {
        this(classLoader, prefix, DEFAULT_SUFFIX);
    }

    public ClassPathTemplateLoader(ClassLoader classLoader, final String prefix, final String suffix) {
        this.classLoader = classLoader;
        setPrefix(prefix);
        setSuffix(suffix);
    }

    @Override
    protected URL getResource(final String location) {
        String packageName = getClass().getPackage().getName().replace('.', '/');
        String res = location.startsWith("/") ? location.substring(1) : packageName + '/' + location;
        return classLoader.getResource(res);
    }
}
