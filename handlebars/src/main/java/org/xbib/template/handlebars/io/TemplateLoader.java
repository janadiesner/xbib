
package org.xbib.template.handlebars.io;

import java.io.IOException;

/**
 * <p>
 * Strategy interface for loading resources from class path, file system, etc.
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
 */
public interface TemplateLoader {

    /**
     * The default view prefix.
     */
    String DEFAULT_PREFIX = "/";

    /**
     * The default view suffix.
     */
    String DEFAULT_SUFFIX = ".hbs";

    /**
     * Get a template source from location.
     *
     * @param location The location of the template source. Required.
     * @return A new template source.
     * @throws java.io.IOException If the template's source can't be resolved.
     */
    TemplateSource sourceAt(final String location) throws IOException;

    /**
     * Resolve a relative location to an absolute location.
     *
     * @param location The candidate location.
     * @return Resolve the uri to an absolute location.
     */
    String resolve(final String location);

    /**
     * @return The prefix that gets prepended to view names when building a URI.
     */
    String getPrefix();

    /**
     * @return The suffix that gets appended to view names when building a URI.
     */
    String getSuffix();

}
