
package org.xbib.template.handlebars;

import java.io.IOException;
import java.io.Writer;

/**
 * Make handlebars templates type-safe. Users can extend the {@link TypeSafeTemplate} and add new
 * methods.
 * <p>
 * Usage:
 * </p>
 * <p/>
 * <pre>
 *  public interface UserTemplate extends TypeSafeTemplate<User> {
 *    UserTemplate setAge(int age);
 *
 *    UserTemplate setRole(String role);
 *
 *    ...
 *  }
 *
 *  UserTemplate template = new Handlebars().compileInline("{{name}} is {{age}} years old!")
 *    .as(UserTemplate.class);
 *
 *  template.setAge(32);
 *
 *  assertEquals("Edgar is 32 years old!", template.apply(new User("Edgar")));
 * </pre>
 *
 * @param <T> The root object type.
 * @see Template#as(Class)
 * @see Template#as()
 */
public interface TypeSafeTemplate<T> {

    /**
     * Merge the template tree using the given context.
     *
     * @param context The context object. May be null.
     * @param writer  The writer object. Required.
     * @throws java.io.IOException If a resource cannot be loaded.
     */
    void apply(T context, Writer writer) throws IOException;

    /**
     * Merge the template tree using the given context.
     *
     * @param context The context object. May be null.
     * @return The resulting template.
     * @throws java.io.IOException If a resource cannot be loaded.
     */
    String apply(T context) throws IOException;

}
