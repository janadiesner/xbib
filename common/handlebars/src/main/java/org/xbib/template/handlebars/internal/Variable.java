
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.HandlebarsContext;
import org.xbib.template.handlebars.EscapingStrategy;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Lambda;
import org.xbib.template.handlebars.MissingValueResolver;
import org.xbib.template.handlebars.Options;
import org.xbib.template.handlebars.TagType;
import org.xbib.template.handlebars.Template;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The most basic tag type is the variable. A {{name}} tag in a basic template
 * will try to find the name key in the current context. If there is no name
 * key, nothing will be rendered.
 * All variables are HTML escaped by default. If you want to return unescaped
 * HTML, use the triple mustache: {{{name}}}.
 * You can also use & to unescape a variable: {{& name}}. This may be useful
 * when changing delimiters.
 */
class Variable extends HelperResolver {

    /**
     * The variable's name. Required.
     */
    private final String name;

    /**
     * The variable type.
     */
    private final TagType type;

    /**
     * Default value for a variable. If set, no lookup is executed. Optional.
     */
    private final Object constant;

    /**
     * The missing value resolver strategy.
     */
    private MissingValueResolver missingValueResolver;

    /**
     * The start delimiter.
     */
    private String startDelimiter;

    /**
     * The end delimiter.
     */
    private String endDelimiter;

    /**
     * The escaping strategy.
     */
    private EscapingStrategy escapingStrategy;

    /**
     * Creates a new {@link org.xbib.template.handlebars.internal.Variable}.
     *
     * @param handlebars The handlebars instance.
     * @param name       The variable's name. Required.
     * @param type       The variable's type. Required.
     * @param params     The variable's parameters. Required.
     * @param hash       The variable's hash. Required.
     */
    public Variable(final Handlebars handlebars, final String name,
                    final TagType type, final List<Object> params,
                    final Map<String, Object> hash) {
        this(handlebars, name, null, type, params, hash);
    }

    /**
     * Creates a new {@link org.xbib.template.handlebars.internal.Variable}.
     *
     * @param handlebars The handlebars instance.
     * @param name       The variable's name. Required.
     * @param value      The variable's value. Optional.
     * @param type       The variable's type. Required.
     * @param params     The variable's parameters. Required.
     * @param hash       The variable's hash. Required.
     */
    public Variable(final Handlebars handlebars, final String name,
                    final Object value, final TagType type, final List<Object> params,
                    final Map<String, Object> hash) {
        super(handlebars);
        this.missingValueResolver = handlebars.getMissingValueResolver();
        this.escapingStrategy = handlebars.getEscapingStrategy();
        this.name = name.trim();
        this.constant = value;
        this.type = type;
        params(params);
        hash(hash);
    }

    /**
     * Creates a new {@link org.xbib.template.handlebars.internal.Variable}.
     *
     * @param handlebars The handlebars instance.
     * @param name       The variable's name. Required.
     * @param value      The variable's value. Optional.
     * @param type       The variable's type. Required.
     */
    @SuppressWarnings("unchecked")
    public Variable(final Handlebars handlebars, final String name,
                    final Object value, final TagType type) {
        this(handlebars, name, value, type, Collections.EMPTY_LIST,
                Collections.EMPTY_MAP);
    }

    /**
     * The variable's name.
     *
     * @return The variable's name.
     */
    public String name() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void merge(final HandlebarsContext scope, final Writer writer)
            throws IOException {
        Helper<Object> helper = helper(name);
        if (helper != null) {
            Options options = new Options.Builder(handlebars, type, scope, Template.EMPTY)
                    .setParams(params(scope))
                    .setHash(hash(scope))
                    .build();
            options.data(HandlebarsContext.PARAM_SIZE, this.params.size());
            CharSequence result = helper.apply(determineContext(scope), options);
            if (escape(result)) {
                writer.append(escapingStrategy.escape(result));
            } else if (result != null) {
                writer.append(result);
            }
        } else {
            Object value = constant == null ? scope.get(name) : constant;
            if (value == null) {
                value = missingValueResolver.resolve(determineContext(scope), name);
            }
            if (value != null) {
                if (value instanceof Lambda) {
                    value =
                            Lambdas.merge(handlebars, (Lambda<Object, Object>) value, scope,
                                    this);
                }
                String stringValue = value.toString();
                // TODO: Add formatter hook
                if (escape(value)) {
                    writer.append(escapingStrategy.escape(stringValue));
                } else {
                    // DON'T escape none String values.
                    writer.append(stringValue);
                }
            }
        }
    }

    @Override
    protected void collect(final Collection<String> result, final TagType tagType) {
        if (this.type == tagType) {
            result.add(name);
        }
    }

    /**
     * True if the given value should be escaped.
     *
     * @param value The variable's value.
     * @return True if the given value should be escaped.
     */
    private boolean escape(final Object value) {
        return !(value instanceof Handlebars.SafeString) && type == TagType.VAR;
    }

    @Override
    public String text() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(startDelimiter).append(name);
        String params = paramsToString();
        if (params.length() > 0) {
            buffer.append(" ").append(params);
        }
        String hash = hashToString();
        if (hash.length() > 0) {
            buffer.append(" ").append(hash);
        }
        return buffer.append(endDelimiter).toString();
    }

    /**
     * Set the end delimiter.
     *
     * @param endDelimiter The end delimiter.
     * @return This section.
     */
    public Variable endDelimiter(final String endDelimiter) {
        this.endDelimiter = endDelimiter;
        return this;
    }

    /**
     * Set the start delimiter.
     *
     * @param startDelimiter The start delimiter.
     * @return This section.
     */
    public Variable startDelimiter(final String startDelimiter) {
        this.startDelimiter = startDelimiter;
        return this;
    }

    /**
     * The start delimiter.
     *
     * @return The start delimiter.
     */
    public String startDelimiter() {
        return startDelimiter;
    }

    /**
     * The end delimiter.
     *
     * @return The end delimiter.
     */
    public String endDelimiter() {
        return endDelimiter;
    }

}
