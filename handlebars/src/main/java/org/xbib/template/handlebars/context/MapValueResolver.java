
package org.xbib.template.handlebars.context;

import org.xbib.template.handlebars.ValueResolver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A {@link java.util.Map} value resolver.
 */
public enum MapValueResolver implements ValueResolver {

    /**
     * A singleton instance.
     */
    INSTANCE;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object resolve(final Object context, final String name) {
        Object value = null;
        if (context instanceof Map) {
            value = ((Map) context).get(name);
            // fallback to EnumMap
            if (value == null && context instanceof EnumMap) {
                EnumMap emap = (EnumMap) context;
                if (emap.size() > 0) {
                    Enum first = (Enum) emap.keySet().iterator().next();
                    Enum key = Enum.valueOf(first.getClass(), name);
                    value = emap.get(key);
                }
            }
        }
        return value == null ? UNRESOLVED : value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Set<Entry<String, Object>> propertySet(final Object context) {
        if (context instanceof Map) {
            return ((Map) context).entrySet();
        }
        return Collections.emptySet();
    }
}
