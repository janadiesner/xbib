package org.xbib.template.handlebars.helper;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Number format styles.
 *
 */
enum NumberStyle {

    /**
     * The default number format.
     */
    DEFAULT {
        @Override
        public NumberFormat numberFormat(final Locale locale) {
            return NumberFormat.getInstance(locale);
        }
    },

    /**
     * The integer number format.
     */
    INTEGER {
        @Override
        public NumberFormat numberFormat(final Locale locale) {
            return NumberFormat.getIntegerInstance(locale);
        }
    },

    /**
     * The currency number format.
     */
    CURRENCY {
        @Override
        public NumberFormat numberFormat(final Locale locale) {
            return NumberFormat.getCurrencyInstance(locale);
        }
    },

    /**
     * The percent number format.
     */
    PERCENT {
        @Override
        public NumberFormat numberFormat(final Locale locale) {
            return NumberFormat.getPercentInstance(locale);
        }
    };

    /**
     * Build a new number format.
     *
     * @param locale The locale to use.
     * @return A new number format.
     */
    public abstract NumberFormat numberFormat(Locale locale);
}