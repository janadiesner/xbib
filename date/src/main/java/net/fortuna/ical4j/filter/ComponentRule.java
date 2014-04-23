
package net.fortuna.ical4j.filter;

import net.fortuna.ical4j.model.Component;

/**
 * An abstract rule implementation serving as the base class for component matching rule implementations.
 */
public abstract class ComponentRule implements Rule {

    public final boolean match(final Object o) {
        return match((Component) o);
    }

    /**
     * @param component a component to match on
     * @return true if the component matches the rule, otherwise false
     */
    public abstract boolean match(Component component);
}
