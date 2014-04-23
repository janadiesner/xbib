
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.HandlebarsContext;

import java.io.IOException;
import java.io.Writer;

import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * Plain text template.
 */
class Text extends BaseTemplate {

    /**
     * The plain text. Required.
     */
    private String text;

    /**
     * Creates a new {@link org.xbib.template.handlebars.internal.Text}.
     *
     * @param text The text content. Required.
     */
    public Text(final String text) {
        this.text = notNull(text, "The text content is required.");
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    protected void merge(final HandlebarsContext scope, final Writer writer)
            throws IOException {
        writer.append(text);
    }

    /**
     * Append text.
     *
     * @param text The text to append.
     * @return This object.
     */
    public Text append(final String text) {
        this.text += text;
        return this;
    }

}
