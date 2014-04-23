
package org.xbib.template.handlebars.util;

import java.lang.reflect.Array;
import java.util.Collection;

public class ObjectUtil {

    /**
     * Evaluate the given object and return true is the object is considered
     * empty. Nulls, empty list or array and false values are considered empty.
     *
     * @param value The object value.
     * @return Return true is the object is considered empty. Nulls, empty list
     * or array and false values are considered empty.
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(final Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        }
        if (value instanceof Collection) {
            return ((Collection) value).size() == 0;
        }
        if (value instanceof Iterable) {
            return !((Iterable) value).iterator().hasNext();
        }
        if (value instanceof Boolean) {
            return !(Boolean) value;
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        return value instanceof Number && ((Number) value).intValue() == 0;
    }
}
