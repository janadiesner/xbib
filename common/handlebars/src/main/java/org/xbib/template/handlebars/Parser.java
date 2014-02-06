
package org.xbib.template.handlebars;

import org.xbib.template.handlebars.io.TemplateSource;

import java.io.IOException;

/**
 * The Handlebars Parser.
 */
public interface Parser {

    /**
     * Parse a handlebars input and return a {@link Template}.
     *
     * @param source The input to parse. Required.
     * @return A new handlebars template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    Template parse(TemplateSource source) throws IOException;
}
