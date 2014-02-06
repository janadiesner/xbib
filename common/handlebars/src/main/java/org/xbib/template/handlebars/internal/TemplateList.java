
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.Context;
import org.xbib.template.handlebars.TagType;
import org.xbib.template.handlebars.Template;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A list of templates.
 */
class TemplateList extends BaseTemplate implements Iterable<Template> {

    /**
     * The list of child templates.
     */
    private final List<Template> nodes = new LinkedList<Template>();

    /**
     * Add a child template. Empty templates aren't added.
     *
     * @param child The child template.
     * @return True, if the template was added.
     */
    public boolean add(final Template child) {
        nodes.add(child);
        return true;
    }

    @Override
    protected void merge(final Context context, final Writer writer)
            throws IOException {
        for (Template node : nodes) {
            node.apply(context, writer);
        }
    }

    @Override
    public String text() {
        StringBuilder buffer = new StringBuilder();
        for (Template node : nodes) {
            buffer.append(node.text());
        }
        return buffer.toString();
    }

    @Override
    public Iterator<Template> iterator() {
        return nodes.iterator();
    }

    /**
     * The number of children.
     *
     * @return The number of children.
     */
    public int size() {
        return nodes.size();
    }

    @Override
    public List<String> collect(final TagType... tagType) {
        Set<String> tagNames = new LinkedHashSet<String>();
        for (Template node : nodes) {
            tagNames.addAll(node.collect(tagType));
        }
        return new ArrayList<String>(tagNames);
    }
}
