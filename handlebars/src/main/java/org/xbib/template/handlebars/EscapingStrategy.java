
package org.xbib.template.handlebars;

import org.xbib.template.handlebars.util.StringUtil;

/**
 * <p>
 * A strategy for determining how to escape a variable (<code>{{variable}}</code>)..
 * </p>
 * <p>
 * Usage:
 * </p>
 * <p/>
 * <pre>
 *    EscapingStrategy escapingStrategy = new EscapingStrategy() {
 *       public String escape(final CharSequence value) {
 *         // return the character sequence escaped however you want
 *       }
 *    };
 *    Handlebars handlebars = new Handlebars().with(escapingStrategy);
 * </pre>
 */
public interface EscapingStrategy {

    /**
     * The default HTML Entity escaping strategy.
     */
    EscapingStrategy HTML_ENTITY = new EscapingStrategy() {
        @Override
        public String escape(final CharSequence value) {
            return value == null ? "" : StringUtil.htmlEscape(value.toString());
        }
    };

    /**
     * Escape variable for CSV.
     */
    EscapingStrategy CSV = new EscapingStrategy() {
        @Override
        public String escape(final CharSequence value) {
            return value == null ? "" : StringUtil.csvEscape(value.toString());
        }
    };

    /**
     * Escape variable for XML.
     */
    EscapingStrategy XML = new EscapingStrategy() {
        @Override
        public String escape(final CharSequence value) {
            return value == null ? "" : StringUtil.xmlEscape((value.toString()));
        }
    };

    /**
     * Escape variable for JavaScript.
     */
    EscapingStrategy JS = new EscapingStrategy() {
        @Override
        public String escape(final CharSequence value) {
            return value == null ? "" : StringUtil.javaScriptEscape(value.toString());
        }
    };

    /**
     * No escape
     */
    EscapingStrategy NONE = new EscapingStrategy() {
        @Override
        public String escape(final CharSequence value) {
            return value == null ? "" : value.toString();
        }
    };

    /**
     * Escape the {@link CharSequence}.
     *
     * @param value the character sequence to be escaped.
     * @return the escaped character sequence.
     */
    String escape(CharSequence value);

}
