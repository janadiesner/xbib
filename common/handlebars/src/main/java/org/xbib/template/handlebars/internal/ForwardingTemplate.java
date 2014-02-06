
package org.xbib.template.handlebars.internal;


import org.xbib.template.handlebars.Context;
import org.xbib.template.handlebars.TagType;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.TypeSafeTemplate;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * A forwarding template implementation.
 */
class ForwardingTemplate implements Template {

    /**
     * The original template.
     */
    private final Template template;

    /**
     * Creates a new {@link org.xbib.template.handlebars.internal.ForwardingTemplate}.
     *
     * @param template The original template. Required.
     */
    public ForwardingTemplate(final Template template) {
        this.template = notNull(template, "The template is required.");
    }

    @Override
    public void apply(final Object context, final Writer writer) throws IOException {
        apply(wrap(context), writer);
    }

    @Override
    public String apply(final Object context) throws IOException {
        return apply(wrap(context));
    }

    @Override
    public void apply(final Context context, final Writer writer) throws IOException {
        Context wrappedContext = wrap(context);
        try {
            beforeApply(wrappedContext);
            template.apply(wrappedContext, writer);
        } finally {
            afterApply(wrappedContext);
            if (wrappedContext != context) {
                wrappedContext.destroy();
            }
        }
    }

    @Override
    public String apply(final Context context) throws IOException {
        Context wrappedContext = wrap(context);
        try {
            beforeApply(wrappedContext);
            return template.apply(wrappedContext);
        } finally {
            afterApply(wrappedContext);
            if (wrappedContext != context) {
                wrappedContext.destroy();
            }
        }
    }

    /**
     * Call it after a template has been applied.
     *
     * @param context The template context.
     */
    protected void afterApply(final Context context) {
    }

    /**
     * Call it before a template has been applied.
     *
     * @param context The template context.
     */
    protected void beforeApply(final Context context) {
    }

    @Override
    public String text() {
        return template.text();
    }

    @Override
    public <T, S extends TypeSafeTemplate<T>> S as(final Class<S> type) {
        return template.as(type);
    }

    @Override
    public <T> TypeSafeTemplate<T> as() {
        return template.as();
    }

    @Override
    public String toString() {
        return template.toString();
    }

    /**
     * Wrap the candidate object as a Context, or creates a new context.
     *
     * @param candidate The candidate object.
     * @return A context.
     */
    private static Context wrap(final Object candidate) {
        if (candidate instanceof Context) {
            return (Context) candidate;
        }
        return Context.newContext(candidate);
    }

    @Override
    public List<String> collect(final TagType... tagType) {
        return template.collect(tagType);
    }
}
