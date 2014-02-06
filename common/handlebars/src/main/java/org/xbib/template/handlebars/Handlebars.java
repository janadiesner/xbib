
package org.xbib.template.handlebars;

import org.xbib.template.handlebars.cache.NullTemplateCache;
import org.xbib.template.handlebars.cache.TemplateCache;
import org.xbib.template.handlebars.helper.DefaultHelperRegistry;
import org.xbib.template.handlebars.internal.HbsParserFactory;
import org.xbib.template.handlebars.io.ClassPathTemplateLoader;
import org.xbib.template.handlebars.io.CompositeTemplateLoader;
import org.xbib.template.handlebars.io.StringTemplateSource;
import org.xbib.template.handlebars.io.TemplateLoader;
import org.xbib.template.handlebars.io.TemplateSource;
import org.xbib.template.handlebars.util.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <p>
 * Handlebars provides the power necessary to let you build semantic templates effectively with no
 * frustration.
 * </p>
 * <h2>
 * Getting Started:</h2>
 * <p/>
 * <pre>
 * Handlebars handlebars = new Handlebars();
 * Template template = handlebars.compileInline("Hello {{this}}!");
 * String s = template.apply("Handlebars.java");
 * </pre>
 * <p/>
 * <h2>Loading templates</h2> Templates are loaded using the ```TemplateLoader``` class.
 * Handlebars.java provides three implementations of a ```TemplateLodaer```:
 * <ul>
 * <li>ClassPathTemplateLoader (default)</li>
 * <li>FileTemplateLoader</li>
 * <li>SpringTemplateLoader (available at the handlebars-springmvc module)</li>
 * </ul>
 * <p/>
 * <p>
 * This example load <code>mytemplate.hbs</code> from the root of the classpath:
 * </p>
 * <p/>
 * <pre>
 * Handlebars handlebars = new Handlebars();
 *
 * Template template = handlebars.compileInline(URI.create("mytemplate"));
 *
 * String s = template.apply("Handlebars.java");
 * </pre>
 * <p/>
 * <p>
 * You can specify a different ```TemplateLoader``` by:
 * </p>
 * <p/>
 * <pre>
 * TemplateLoader loader = ...;
 * Handlebars handlebars = new Handlebars(loader);
 * </pre>
 */
public class Handlebars implements HelperRegistry {

    /**
     * A {@link Handlebars.SafeString} tell {@link Handlebars} that the content should not be
     * escaped as HTML.
     */
    public static class SafeString implements CharSequence {

        /**
         * The content.
         */
        private CharSequence content;

        /**
         * Creates a new {@link Handlebars.SafeString}.
         *
         * @param content The string content.
         */
        public SafeString(final CharSequence content) {
            this.content = content;
        }

        @Override
        public int length() {
            return content.length();
        }

        @Override
        public char charAt(final int index) {
            return content.charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return content.subSequence(start, end);
        }

        @Override
        public String toString() {
            return content.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (content == null ? 0
                    : content.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof SafeString) {
                SafeString that = (SafeString) obj;
                return content.equals(that.content);
            }
            return false;
        }
    }

    /**
     * The default start delimiter.
     */
    public static final String DELIM_START = "{{";

    /**
     * The default end delimiter.
     */
    public static final String DELIM_END = "}}";

    /**
     * The template loader. Required.
     */
    private TemplateLoader loader;

    /**
     * The template cache. Required.
     */
    private TemplateCache cache = NullTemplateCache.INSTANCE;

    /**
     * If true, missing helper parameters will be resolve to their names.
     */
    private boolean stringParams;

    /**
     * If true, unnecessary whitespace and new lines will be removed.
     */
    private boolean prettyPrint;

    /**
     * The helper registry.
     */
    private HelperRegistry registry = new DefaultHelperRegistry();

    /**
     * If true, templates will be able to call him self directly or indirectly. Use with caution.
     * Default is: false.
     */
    private boolean infiniteLoops;

    /**
     * The missing value resolver strategy.
     */
    private MissingValueResolver missingValueResolver = MissingValueResolver.NULL;

    /**
     * The escaping strategy.
     */
    private EscapingStrategy escapingStrategy = EscapingStrategy.HTML_ENTITY;

    /**
     * The parser factory. Required.
     */
    private ParserFactory parserFactory = new HbsParserFactory();

    /**
     * The start delimiter.
     */
    private String startDelimiter = DELIM_START;

    /**
     * The end delimiter.
     */
    private String endDelimiter = DELIM_END;

    /**
     * Creates a new {@link Handlebars} with no cache.
     *
     * @param loader The template loader. Required.
     */
    public Handlebars(final TemplateLoader loader) {
        with(loader);
    }

    /**
     * Creates a new {@link Handlebars} with a {@link org.xbib.template.handlebars.io.ClassPathTemplateLoader} and no
     * cache.
     */
    public Handlebars() {
        this(new ClassPathTemplateLoader());
    }

    /**
     * Compile the resource located at the given uri.
     *
     * @param location The resource's location. Required.
     * @return A compiled template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public Template compile(final String location) throws IOException {
        return compile(location, startDelimiter, endDelimiter);
    }

    /**
     * Compile the resource located at the given uri.
     *
     * @param location       The resource's location. Required.
     * @param startDelimiter The start delimiter. Required.
     * @param endDelimiter   The end delimiter. Required.
     * @return A compiled template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public Template compile(final String location, final String startDelimiter,
                            final String endDelimiter) throws IOException {
        return compile(loader.sourceAt(location), startDelimiter, endDelimiter);
    }

    /**
     * Compile a handlebars template.
     *
     * @param input The handlebars input. Required.
     * @return A compiled template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public Template compileInline(final String input) throws IOException {
        return compileInline(input, startDelimiter, endDelimiter);
    }

    /**
     * Compile a handlebars template.
     *
     * @param input          The input text. Required.
     * @param startDelimiter The start delimiter. Required.
     * @param endDelimiter   The end delimiter. Required.
     * @return A compiled template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public Template compileInline(final String input, final String startDelimiter,
                                  final String endDelimiter) throws IOException {
        Validate.notNull(input, "The input is required.");
        String filename = "inline@" + Integer.toHexString(Math.abs(input.hashCode()));
        return compile(new StringTemplateSource(loader.resolve(filename), input),
                startDelimiter, endDelimiter);
    }

    /**
     * Compile a handlebars template.
     *
     * @param source The template source. Required.
     * @return A handlebars template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public Template compile(final TemplateSource source) throws IOException {
        return compile(source, startDelimiter, endDelimiter);
    }

    /**
     * Compile a handlebars template.
     *
     * @param source         The template source. Required.
     * @param startDelimiter The start delimiter. Required.
     * @param endDelimiter   The end delimiter. Required.
     * @return A handlebars template.
     * @throws java.io.IOException If the resource cannot be loaded.
     */
    public Template compile(final TemplateSource source, final String startDelimiter,
                            final String endDelimiter) throws IOException {
        Validate.notNull(source, "The template source is required.");
        Validate.notEmpty(startDelimiter, "The start delimiter is required.");
        Validate.notEmpty(endDelimiter, "The end delimiter is required.");
        Parser parser = parserFactory.create(this, startDelimiter, endDelimiter);
        return cache.get(source, parser);
    }

    /**
     * Find a helper by name.
     *
     * @param <C>  The helper runtime type.
     * @param name The helper's name. Required.
     * @return A helper or null if it's not found.
     */
    @Override
    public <C> Helper<C> helper(final String name) {
        return registry.helper(name);
    }

    /**
     * Register a helper in the helper registry.
     *
     * @param <H>    The helper runtime type.
     * @param name   The helper's name. Required.
     * @param helper The helper object. Required.
     * @return This handlebars.
     */
    @Override
    public <H> Handlebars registerHelper(final String name, final Helper<H> helper) {
        registry.registerHelper(name, helper);
        return this;
    }

    /**
     * <p>
     * Register all the helper methods for the given helper source.
     * </p>
     * <p>
     * A helper method looks like:
     * </p>
     * <p/>
     * <pre>
     * public static? CharSequence methodName(context?, parameter*, options?) {
     * }
     * </pre>
     * <p/>
     * Where:
     * <ul>
     * <li>A method can/can't be static</li>
     * <li>The method's name became the helper's name</li>
     * <li>Context, parameters and options are all optional</li>
     * <li>If context and options are present they must be the first and last method arguments.</li>
     * </ul>
     * <p/>
     * Instance and static methods will be registered as helpers.
     *
     * @param helperSource The helper source. Required.
     * @return This handlebars object.
     */
    @Override
    public Handlebars registerHelpers(final Object helperSource) {
        registry.registerHelpers(helperSource);
        return this;
    }

    /**
     * <p>
     * Register all the helper methods for the given helper source.
     * </p>
     * <p>
     * A helper method looks like:
     * </p>
     * <p/>
     * <pre>
     * public static? CharSequence methodName(context?, parameter*, options?) {
     * }
     * </pre>
     * <p/>
     * Where:
     * <ul>
     * <li>A method can/can't be static</li>
     * <li>The method's name became the helper's name</li>
     * <li>Context, parameters and options are all optional</li>
     * <li>If context and options are present they must be the first and last method arguments.</li>
     * </ul>
     * <p/>
     * Only static methods will be registered as helpers.
     * <p>Enums are supported too</p>
     *
     * @param helperSource The helper source. Enums are supported. Required.
     * @return This handlebars object.
     */
    @Override
    public Handlebars registerHelpers(final Class<?> helperSource) {
        registry.registerHelpers(helperSource);
        return this;
    }

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param location A classpath location. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    @Override
    public Handlebars registerHelpers(final URI location) throws Exception {
        registry.registerHelpers(location);
        return this;
    }

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param input A JavaScript file name. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    @Override
    public Handlebars registerHelpers(final File input) throws Exception {
        registry.registerHelpers(input);
        return this;
    }

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param filename The file name (just for debugging purpose). Required.
     * @param source   The JavaScript source. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    @Override
    public Handlebars registerHelpers(final String filename, final Reader source) throws Exception {
        registry.registerHelpers(filename, source);
        return this;
    }

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param filename The file name (just for debugging purpose). Required.
     * @param source   The JavaScript source. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    @Override
    public Handlebars registerHelpers(final String filename, final InputStream source)
            throws Exception {
        registry.registerHelpers(filename, source);
        return this;
    }

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param filename The file name (just for debugging purpose). Required.
     * @param source   The JavaScript source. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    @Override
    public Handlebars registerHelpers(final String filename, final String source) throws Exception {
        registry.registerHelpers(filename, source);
        return this;
    }

    @Override
    public Set<Entry<String, Helper<?>>> helpers() {
        return registry.helpers();
    }

    /**
     * The resource locator.
     *
     * @return The resource locator.
     */
    public TemplateLoader getLoader() {
        return loader;
    }

    /**
     * The template cache.
     *
     * @return The template cache.
     */
    public TemplateCache getCache() {
        return cache;
    }

    /**
     * The missing value resolver.
     *
     * @return The missing value resolver.
     */
    public MissingValueResolver getMissingValueResolver() {
        return missingValueResolver;
    }

    /**
     * The escaping strategy.
     *
     * @return The escaping strategy.
     */
    public EscapingStrategy getEscapingStrategy() {
        return escapingStrategy;
    }

    /**
     * If true, missing helper parameters will be resolve to their names.
     *
     * @return If true, missing helper parameters will be resolve to their names.
     */
    public boolean stringParams() {
        return stringParams;
    }

    /**
     * If true, unnecessary spaces and new lines will be removed from output. Default is: false.
     *
     * @return If true, unnecessary spaces and new lines will be removed from output. Default is:
     * false.
     */
    public boolean prettyPrint() {
        return prettyPrint;
    }

    /**
     * If true, unnecessary spaces and new lines will be removed from output. Default is: false.
     *
     * @param prettyPrint If true, unnecessary spaces and new lines will be removed from output.
     *                    Default is: false.
     */
    public void setPrettyPrint(final boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * If true, unnecessary spaces and new lines will be removed from output. Default is: false.
     *
     * @param prettyPrint If true, unnecessary spaces and new lines will be removed from output.
     *                    Default is: false.
     * @return This handlebars object.
     */
    public HelperRegistry prettyPrint(final boolean prettyPrint) {
        setPrettyPrint(prettyPrint);
        return this;
    }

    /**
     * If true, missing helper parameters will be resolve to their names.
     *
     * @param stringParams If true, missing helper parameters will be resolve to
     *                     their names.
     */
    public void setStringParams(final boolean stringParams) {
        this.stringParams = stringParams;
    }

    /**
     * If true, missing helper parameters will be resolve to their names.
     *
     * @param stringParams If true, missing helper parameters will be resolve to
     *                     their names.
     * @return The handlebars object.
     */
    public HelperRegistry stringParams(final boolean stringParams) {
        setStringParams(stringParams);
        return this;
    }

    /**
     * If true, templates will be able to call him self directly or indirectly. Use with caution.
     * Default is: false.
     *
     * @return If true, templates will be able to call him self directly or indirectly. Use with
     * caution. Default is: false.
     */
    public boolean infiniteLoops() {
        return infiniteLoops;
    }

    /**
     * If true, templates will be able to call him self directly or indirectly. Use with caution.
     * Default is: false.
     *
     * @param infiniteLoops If true, templates will be able to call him self directly or
     *                      indirectly.
     */
    public void setInfiniteLoops(final boolean infiniteLoops) {
        this.infiniteLoops = infiniteLoops;
    }

    /**
     * If true, templates will be able to call him self directly or indirectly. Use with caution.
     * Default is: false.
     *
     * @param infiniteLoops If true, templates will be able to call him self directly or
     *                      indirectly.
     * @return The handlebars object.
     */
    public HelperRegistry infiniteLoops(final boolean infiniteLoops) {
        setInfiniteLoops(infiniteLoops);
        return this;
    }

    /**
     * Set the end delimiter.
     *
     * @param endDelimiter The end delimiter. Required.
     */
    public void setEndDelimiter(final String endDelimiter) {
        this.endDelimiter = Validate.notEmpty(endDelimiter, "The endDelimiter is required.");
    }

    /**
     * Set the end delimiter.
     *
     * @param endDelimiter The end delimiter. Required.
     * @return This handlebars object.
     */
    public Handlebars endDelimiter(final String endDelimiter) {
        setEndDelimiter(endDelimiter);
        return this;
    }

    /**
     * Set the start delimiter.
     *
     * @param startDelimiter The start delimiter. Required.
     */
    public void setStartDelimiter(final String startDelimiter) {
        this.startDelimiter = Validate.notEmpty(startDelimiter, "The startDelimiter is required.");
    }

    /**
     * Set the start delimiter.
     *
     * @param startDelimiter The start delimiter. Required.
     * @return This handlebars object.
     */
    public Handlebars startDelimiter(final String startDelimiter) {
        setStartDelimiter(startDelimiter);
        return this;
    }

    /**
     * Set one or more {@link org.xbib.template.handlebars.io.TemplateLoader}. In the case of two or more {@link org.xbib.template.handlebars.io.TemplateLoader}, a
     * {@link org.xbib.template.handlebars.io.CompositeTemplateLoader} will be created. Default is: {@link org.xbib.template.handlebars.io.ClassPathTemplateLoader}.
     *
     * @param loader The template loader. Required.
     * @return This handlebars object.
     * @see org.xbib.template.handlebars.io.CompositeTemplateLoader
     */
    public Handlebars with(final TemplateLoader... loader) {
        Validate.isTrue(loader.length > 0, "The template loader is required.");
        this.loader = loader.length == 1 ? loader[0] : new CompositeTemplateLoader(loader);
        return this;
    }

    /**
     * Set a new {@link ParserFactory}.
     *
     * @param parserFactory A parser factory. Required.
     * @return This handlebars object.
     */
    public HelperRegistry with(final ParserFactory parserFactory) {
        this.parserFactory = Validate.notNull(parserFactory, "A parserFactory is required.");
        return this;
    }

    /**
     * Set a new {@link org.xbib.template.handlebars.cache.TemplateCache}.
     *
     * @param cache The template cache. Required.
     * @return This handlebars object.
     */
    public HelperRegistry with(final TemplateCache cache) {
        this.cache = Validate.notNull(cache, "The template loader is required.");
        return this;
    }

    /**
     * Set a new {@link MissingValueResolver}.
     *
     * @param missingValueResolver The missing value resolver. Required.
     * @return This handlebars object.
     */
    public Handlebars with(final MissingValueResolver missingValueResolver) {
        this.missingValueResolver = Validate.notNull(missingValueResolver,
                "The missing value resolver is required.");
        return this;
    }

    /**
     * Set the helper registry. This operation will override will remove any previously registered
     * helper.
     *
     * @param registry The helper registry. Required.
     * @return This handlebars object.
     */
    public Handlebars with(final HelperRegistry registry) {
        this.registry = Validate.notNull(registry, "The registry is required.");

        return this;
    }

    /**
     * Set a new {@link EscapingStrategy}.
     *
     * @param escapingStrategy The escaping strategy. Required.
     * @return This handlebars object.
     */
    public Handlebars with(final EscapingStrategy escapingStrategy) {
        this.escapingStrategy = Validate.notNull(escapingStrategy,
                "The escaping strategy is required.");
        return this;
    }

    /**
     * Return a parser factory.
     *
     * @return A parsert factory.
     */
    public ParserFactory getParserFactory() {
        return parserFactory;
    }

}
