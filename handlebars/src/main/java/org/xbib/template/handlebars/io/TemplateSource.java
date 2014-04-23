
package org.xbib.template.handlebars.io;

import java.io.IOException;
import java.io.Reader;

/**
 * The template source. Implementation of {@link org.xbib.template.handlebars.io.TemplateSource} must implement
 * {@link #equals(Object)} and {@link #hashCode()} methods. This two methods are the core of the
 * cache system.
 */
public interface TemplateSource {

    /**
     * The template content.
     *
     * @return The template content.
     * @throws java.io.IOException If the template can't read.
     */
    String content() throws IOException;

    /**
     * The template content as a {@link java.io.Reader}. Clients of this method must close the {@link java.io.Reader}.
     *
     * @return The template content as a {@link java.io.Reader}.Clients of this method must close the
     * {@link java.io.Reader}.
     * @throws java.io.IOException If the template can't read.
     */
    Reader reader() throws IOException;

    /**
     * The file's name.
     *
     * @return The file's name.
     */
    String filename();

    /**
     * The last modified date.
     *
     * @return The last modified date.
     */
    long lastModified();
}
