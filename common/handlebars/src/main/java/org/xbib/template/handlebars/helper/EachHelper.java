
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.HandlebarsContext;
import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * You can iterate over a list using the built-in each helper. Inside the
 * block, you can use <code>this</code> to reference the element being
 * iterated over.
 */
public class EachHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new EachHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "each";

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        if (context == null) {
            return "";
        }
        if (context instanceof Iterable) {
            return iterableContext((Iterable) context, options);
        }
        if (context instanceof String) {
            return iterableContext(Collections.singleton(context), options);
        }
        return hashContext(context, options);
    }

    /**
     * Iterate over a hash like object.
     *
     * @param context The context object.
     * @param options The helper options.
     * @return The string output.
     * @throws java.io.IOException If something goes wrong.
     */
    private CharSequence hashContext(final Object context, final Options options)
            throws IOException {
        Set<Entry<String, Object>> propertySet = options.propertySet(context);
        StringBuilder buffer = new StringBuilder();
        HandlebarsContext parent = options.context;
        for (Entry<String, Object> entry : propertySet) {
            HandlebarsContext current = HandlebarsContext.newContext(parent, entry.getValue())
                    .data("key", entry.getKey());
            buffer.append(options.fn(current));
        }
        return buffer.toString();
    }

    /**
     * Iterate over an iterable object.
     *
     * @param context The context object.
     * @param options The helper options.
     * @return The string output.
     * @throws java.io.IOException If something goes wrong.
     */
    private CharSequence iterableContext(final Iterable<Object> context, final Options options)
            throws IOException {
        StringBuilder buffer = new StringBuilder();
        if (options.isFalsy(context)) {
            buffer.append(options.inverse());
        } else {
            Iterator<Object> iterator = context.iterator();
            int index = -1;
            HandlebarsContext parent = options.context;
            while (iterator.hasNext()) {
                index += 1;
                Object element = iterator.next();
                boolean first = index == 0;
                boolean even = index % 2 == 0;
                boolean last = !iterator.hasNext();
                HandlebarsContext current = HandlebarsContext.newBuilder(parent, element)
                        .combine("@index", index)
                        .combine("@first", first ? "first" : "")
                        .combine("@last", last ? "last" : "")
                        .combine("@odd", even ? "" : "odd")
                        .combine("@even", even ? "even" : "")
                        .build();
                buffer.append(options.fn(current));
                current.destroy();
            }
        }
        return buffer.toString();
    }

}
