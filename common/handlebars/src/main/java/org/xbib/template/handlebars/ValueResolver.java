
package org.xbib.template.handlebars;

import org.xbib.template.handlebars.context.JavaBeanValueResolver;
import org.xbib.template.handlebars.context.MapValueResolver;

import java.util.Map.Entry;
import java.util.Set;

/**
 * A hook interface for resolving values from the {@link Context context stack}.
 */
public interface ValueResolver {

    /**
     * The default value resolvers.
     */
    ValueResolver[] VALUE_RESOLVERS = {MapValueResolver.INSTANCE,
            JavaBeanValueResolver.INSTANCE};

    /**
     * A mark object.
     */
    Object UNRESOLVED = new Object();

    /**
     * Resolve the attribute's name in the context object. If a {@link #UNRESOLVED} is returned, the
     * {@link Context context stack} will
     * continue with the next value resolver in the chain.
     *
     * @param context The context object. Not null.
     * @param name    The attribute's name. Not null.
     * @return A {@link #UNRESOLVED} is returned, the {@link Context context
     * stack} will continue with the next value resolver in the chain.
     * Otherwise, it returns the associated value.
     */
    Object resolve(Object context, String name);

    /**
     * List all the properties and their values for the given object.
     *
     * @param context The context object. Not null.
     * @return All the properties and their values for the given object.
     */
    Set<Entry<String, Object>> propertySet(Object context);
}
