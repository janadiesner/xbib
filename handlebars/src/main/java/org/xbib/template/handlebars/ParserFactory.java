
package org.xbib.template.handlebars;

/**
 * Creates a new Handlebars parser.
 */
public interface ParserFactory {

    /**
     * Creates a new {@link Parser}.
     *
     * @param handlebars     The parser owner.
     * @param startDelimiter The start delimiter.
     * @param endDelimiter   The end delimiter.
     * @return A new {@link Parser}.
     */
    Parser create(final Handlebars handlebars, final String startDelimiter,
                  final String endDelimiter);
}
