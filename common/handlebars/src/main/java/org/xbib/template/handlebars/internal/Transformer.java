
package org.xbib.template.handlebars.internal;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Transform a value and produce a new value if applies. For example, arrays are
 * converted to list in order to iterate over them using the Iterable interface.
 */
enum Transformer {
    /**
     * No transformation at all.
     */
    NONE,

    /**
     * Convert arrays into list.
     */
    ARRAY {
        @Override
        boolean apply(final Object candidate) {
            return candidate != null && candidate.getClass().isArray();
        }

        @Override
        public Object doTransform(final Object candidate) {
            List<Object> list = Arrays.asList((Object[]) candidate);
            return list;
        }
    };

    /**
     * All the transformer are here except {@link #NONE}.
     */
    private static final EnumSet<Transformer> transformers = EnumSet
            .allOf(Transformer.class);
    ;

    static {
        transformers.remove(NONE);
    }

    /**
     * Return true if the strategy applies for the candidate value.
     *
     * @param candidate The candidate value may be null.
     * @return True if the strategy applies for the candidate value.
     */
    boolean apply(final Object candidate) {
        return false;
    }

    /**
     * Transform the given value into something different or leave it as it is.
     *
     * @param candidate The candidate value, may be null.
     * @return A new value or the original value.
     */
    Object doTransform(final Object candidate) {
        return candidate;
    }

    /**
     * Transform the given value into something different or leave it as it is.
     *
     * @param canidate The candidate value. May be null.
     * @return A transformed value or the original value.
     */
    public static Object transform(final Object canidate) {
        return get(canidate).doTransform(canidate);
    }

    /**
     * Find the best transformer for the given value.
     *
     * @param candidate The candidate value.
     * @return The best transformer for the given value. Not null.
     */
    private static Transformer get(final Object candidate) {
        for (Transformer transformer : transformers) {
            if (transformer.apply(candidate)) {
                return transformer;
            }
        }
        return NONE;
    }
}
