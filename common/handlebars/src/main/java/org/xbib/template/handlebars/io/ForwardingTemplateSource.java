
package org.xbib.template.handlebars.io;

import java.io.IOException;
import java.io.Reader;

import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * A template source which forwards all its method calls to another template source.
 */
public class ForwardingTemplateSource extends AbstractTemplateSource {

    /**
     * The template source.
     */
    private final TemplateSource source;

    /**
     * Creates a new {@link org.xbib.template.handlebars.io.ForwardingTemplateSource}.
     *
     * @param source The template source to forwards all the method calls.
     */
    public ForwardingTemplateSource(final TemplateSource source) {
        this.source = notNull(source, "The source is required.");
    }

    @Override
    public String content() throws IOException {
        return source.content();
    }

    @Override
    public Reader reader() throws IOException {
        return source.reader();
    }

    @Override
    public String filename() {
        return source.filename();
    }

    @Override
    public long lastModified() {
        return source.lastModified();
    }

}
