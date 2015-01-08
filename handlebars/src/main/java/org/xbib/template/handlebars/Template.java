
package org.xbib.template.handlebars;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

/**
 * A compiled template created by {@link Handlebars#compileInline(String)}.
 */
public interface Template {

    /**
     * An empty template implementation.
     */
    Template EMPTY = new Template() {
        @Override
        public String text() {
            return "";
        }

        @Override
        public String apply(final Object context) throws IOException {
            return "";
        }

        @Override
        public String apply(final HandlebarsContext context) throws IOException {
            return "";
        }

        @Override
        public void apply(final HandlebarsContext context, final Writer writer)
                throws IOException {
        }

        @Override
        public void apply(final Object context, final Writer writer)
                throws IOException {
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public <T> TypeSafeTemplate<T> as() {
            return as(TypeSafeTemplate.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T, S extends TypeSafeTemplate<T>> S as(final Class<S> rootType) {
            TypeSafeTemplate<T> template = new TypeSafeTemplate<T>() {
                @Override
                public String apply(final T context) throws IOException {
                    return "";
                }

                @Override
                public void apply(final T context, final Writer writer) throws IOException {
                }
            };
            return (S) template;
        }

        @Override
        public List<String> collect(final TagType... tagType) {
            return Collections.emptyList();
        }
    };

    /**
     * Merge the template tree using the given context.
     *
     * @param context The context object. May be null.
     * @param writer  The writer object. Required.
     * @throws java.io.IOException If a resource cannot be loaded.
     */
    void apply(Object context, Writer writer) throws IOException;

    /**
     * Merge the template tree using the given context.
     *
     * @param context The context object. May be null.
     * @return The resulting template.
     * @throws java.io.IOException If a resource cannot be loaded.
     */
    String apply(Object context) throws IOException;

    /**
     * Merge the template tree using the given context.
     *
     * @param context The context object. Required.
     * @param writer  The writer object. Required.
     * @throws java.io.IOException If a resource cannot be loaded.
     */
    void apply(HandlebarsContext context, Writer writer) throws IOException;

    /**
     * Merge the template tree using the given context.
     *
     * @param context The context object. Required.
     * @return The resulting template.
     * @throws java.io.IOException If a resource cannot be loaded.
     */
    String apply(HandlebarsContext context) throws IOException;

    /**
     * Provide the raw text.
     *
     * @return The raw text.
     */
    String text();

    /**
     * Creates a new {@link TypeSafeTemplate}.
     *
     * @param type The template type. Required.
     * @param <T>  The root type.
     * @param <S>  The template type.
     * @return A new {@link TypeSafeTemplate}.
     */
    <T, S extends TypeSafeTemplate<T>> S as(final Class<S> type);

    /**
     * Creates a new {@link TypeSafeTemplate}.
     *
     * @param <T> The root type.
     * @return A new {@link TypeSafeTemplate}.
     */
    <T> TypeSafeTemplate<T> as();

    /**
     * Collect all the tag names under the given tagType.
     * <p>
     * Usage:
     * </p>
     * <p/>
     * <pre>
     * {{hello}}
     * {{var 1}}
     * {{{tripleVar}}}
     * </pre>
     * <p>
     * <code>collect(TagType.VAR)</code> returns <code>[hello, var]</code>
     * </p>
     * <p>
     * <code>collect(TagType.TRIPLE_VAR)</code> returns <code>[tripleVar]</code>
     * </p>
     * <p>
     * <code>collect(TagType.VAR, TagType.TRIPLE_VAR)</code> returns
     * <code>[hello, var, tripleVar]</code>
     * </p>
     *
     * @param tagType The tag type. Required.
     * @return A list with tag names.
     */
    List<String> collect(TagType... tagType);
}
