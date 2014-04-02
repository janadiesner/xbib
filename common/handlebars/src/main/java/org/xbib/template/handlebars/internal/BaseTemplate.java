
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.HandlebarsContext;
import org.xbib.template.handlebars.HandlebarsError;
import org.xbib.template.handlebars.HandlebarsException;
import org.xbib.template.handlebars.util.StringUtil;
import org.xbib.template.handlebars.TagType;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.TypeSafeTemplate;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xbib.template.handlebars.util.Validate.isTrue;
import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * Base class for {@link Template}.
 */
abstract class BaseTemplate implements Template {

    /**
     * The line of this template.
     */
    protected int line;

    /**
     * The column of this template.
     */
    protected int column;

    /**
     * The file's name.
     */
    protected String filename;


    @Override
    public final String apply(final Object context) throws IOException {
        return apply(wrap(context));
    }

    @Override
    public final void apply(final Object context, final Writer writer)
            throws IOException {
        apply(wrap(context), writer);
    }

    @Override
    public String apply(final HandlebarsContext context) throws IOException {
        FastStringWriter writer = new FastStringWriter();
        try {
            apply(context, writer);
            return writer.toString();
        } finally {
            writer.close();
        }
    }

    @Override
    public void apply(final HandlebarsContext context, final Writer writer)
            throws IOException {
        notNull(writer, "A writer is required.");
        HandlebarsContext wrapped = wrap(context);
        try {
            merge(wrapped, writer);
        } catch (HandlebarsException ex) {
            throw ex;
        } catch (Exception ex) {
            String evidence = toString();
            String reason = ex.toString();
            String message =
                    filename + ":" + line + ":" + column + ": "
                            + reason + "\n";
            message += "    " + StringUtil.join(StringUtil.split(evidence, "\n"), "\n    ");
            HandlebarsError error =
                    new HandlebarsError(filename, line, column, reason, evidence,
                            message);
            HandlebarsException hex = new HandlebarsException(error, ex);
            // Override the stack-trace
            hex.setStackTrace(ex.getStackTrace());
            throw hex;
        } finally {
            if (wrapped != context) {
                wrapped.destroy();
            }
        }
    }

    /**
     * Wrap the candidate object as a Context, or creates a new context.
     *
     * @param candidate The candidate object.
     * @return A context.
     */
    private static HandlebarsContext wrap(final Object candidate) {
        if (candidate instanceof HandlebarsContext) {
            return (HandlebarsContext) candidate;
        }
        return HandlebarsContext.newContext(candidate);
    }

    /**
     * Merge a child template into the writer.
     *
     * @param context The scope object.
     * @param writer  The writer.
     * @throws java.io.IOException If a resource cannot be loaded.
     */
    protected abstract void merge(final HandlebarsContext context, Writer writer)
            throws IOException;

    @Override
    public String toString() {
        return text();
    }

    /**
     * Set the file's name.
     *
     * @param filename The file's name.
     * @return This template.
     */
    public BaseTemplate filename(final String filename) {
        this.filename = filename;
        return this;
    }

    /**
     * Set the template position.
     *
     * @param line   The line.
     * @param column The column.
     * @return This template.
     */
    public BaseTemplate position(final int line, final int column) {
        this.line = line;
        this.column = column;
        return this;
    }

    @Override
    public <T, S extends TypeSafeTemplate<T>> S as(final Class<S> rootType) {
        notNull(rootType, "The rootType can't be null.");
        isTrue(rootType.isInterface(), "Not an interface: %s", rootType.getName());
        @SuppressWarnings("unchecked")
        S template = (S) newTypeSafeTemplate(rootType, this);
        return template;
    }

    @Override
    public <T> TypeSafeTemplate<T> as() {
        @SuppressWarnings("unchecked")
        TypeSafeTemplate<T> template = (TypeSafeTemplate<T>) newTypeSafeTemplate(
                TypeSafeTemplate.class, this);
        return template;
    }

    /**
     * Creates a new {@link TypeSafeTemplate}.
     *
     * @param rootType The target type.
     * @param template The target template.
     * @return A new {@link TypeSafeTemplate}.
     */
    private static Object newTypeSafeTemplate(final Class<?> rootType, final Template template) {
        return Proxy.newProxyInstance(template.getClass().getClassLoader(), new Class[]{rootType},
                new InvocationHandler() {
                    private Map<String, Object> attributes = new HashMap<String, Object>();

                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args)
                            throws IOException {
                        String methodName = method.getName();
                        if ("apply".equals(methodName)) {
                            HandlebarsContext context = HandlebarsContext.newBuilder(args[0])
                                    .combine(attributes)
                                    .build();
                            attributes.clear();
                            if (args.length == 2) {
                                template.apply(context, (Writer) args[1]);
                                return null;
                            }
                            return template.apply(context);
                        }

                        if (Modifier.isPublic(method.getModifiers()) && methodName.startsWith("set")) {
                            String attrName = StringUtil.uncapitalize(methodName.substring("set".length()));
                            if (args != null && args.length == 1 && attrName.length() > 0) {
                                attributes.put(attrName, args[0]);
                                if (TypeSafeTemplate.class.isAssignableFrom(method.getReturnType())) {
                                    return proxy;
                                }
                                return null;
                            }
                        }
                        String message = String.format(
                                "No handler method for: '%s(%s)', expected method signature is: 'setXxx(value)'",
                                methodName, args == null ? "" : StringUtil.join(args, ", "));
                        throw new UnsupportedOperationException(message);
                    }
                });
    }

    @Override
    public List<String> collect(final TagType... tagType) {
        isTrue(tagType.length > 0, "At least one tag type is required.");
        Set<String> tagNames = new LinkedHashSet<String>();
        for (TagType tt : tagType) {
            collect(tagNames, tt);
        }
        return new ArrayList<String>(tagNames);
    }

    /**
     * Child classes might want to check if they apply to the tagtype and append them self to the
     * result list.
     *
     * @param result  The result list.
     * @param tagType The matching tagtype.
     */
    protected void collect(final Collection<String> result, final TagType tagType) {
    }

}
