
package org.xbib.template.handlebars.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.xbib.template.handlebars.util.Validate.notNull;


/**
 * String implementation of {@link org.xbib.template.handlebars.io.TemplateSource}.
 */
public class StringTemplateSource extends AbstractTemplateSource {

    /**
     * The template's content. Required.
     */
    private final String content;

    /**
     * The template's file name. Required.
     */
    private final String filename;

    /**
     * The last modified date.
     */
    private final long lastModified;

    /**
     * Creates a new {@link org.xbib.template.handlebars.io.StringTemplateSource}.
     *
     * @param filename The template's file name. Required.
     * @param content  The template's content. Required.
     */
    public StringTemplateSource(final String filename, final String content) {
        this.content = notNull(content, "The content is required.");
        this.filename = notNull(filename, "The filename is required.");
        this.lastModified = content.hashCode();
    }

    @Override
    public String content() throws IOException {
        return content;
    }

    @Override
    public Reader reader() throws IOException {
        return new StringReader(content);
    }

    @Override
    public String filename() {
        return filename;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

}
