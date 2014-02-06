
package org.xbib.template.handlebars;

/**
 * Tags are indicated by the double mustaches.
 */
public enum TagType {
    /**
     * The most basic tag type is the variable. A <code>{{name}}</code> tag in a basic template will
     * try to find the name key in the current context. If there is no name key, nothing will be
     * rendered.
     */
    VAR,

    /**
     * All variables are HTML escaped by default. If you want to return unescaped HTML, use the
     * triple mustache: <code>{{&name}}</code>.
     */
    AMP_VAR,

    /**
     * All variables are HTML escaped by default. If you want to return unescaped HTML, use the
     * triple mustache: <code>{{{name}}}</code>.
     */
    TRIPLE_VAR,

    /**
     * <p>
     * Sections render blocks of text one or more times, depending on the value of the key in the
     * current context.
     * </p>
     * <p/>
     * <p>
     * A section begins with a pound and ends with a slash. That is, {{#person}} begins a "person"
     * section while {{/person}} ends it.
     * </p>
     */
    SECTION {
        @Override
        public boolean inline() {
            return false;
        }
    };

    /**
     * True for inline tags.
     *
     * @return True for inline tags.
     */
    public boolean inline() {
        return true;
    }
}
