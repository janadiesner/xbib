
package org.xbib.template.handlebars;

/**
 * <p>
 * A strategy for dealing with missing values in <code>{{variable}}</code> expression. Useful for
 * using default values and debugging an object graph.
 * </p>
 * <p>
 * Usage:
 * </p>
 * <p/>
 * <pre>
 *    MissingValueResolver missingValue = new MissingValueResolver() {
 *       public String resolve(Object context, String name) {
 *          //return a default value or throw an exception
 *          ...;
 *       }
 *    };
 *    Handlebars handlebars = new Handlebars().with(missingValue);
 * </pre>
 */
public interface MissingValueResolver {

    /**
     * The default missing value resolver.
     */
    MissingValueResolver NULL = new MissingValueResolver() {
        @Override
        public String resolve(final Object context, final String var) {
            return null;
        }
    };

    /**
     * Resolve a missing variable by returning a default value or producing an error.
     *
     * @param context The context object. Might be null.
     * @param var     The variable's name. Never null.
     * @return Resolve a missing variable by returning a default value or producing an error.
     */
    String resolve(Object context, String var);
}
