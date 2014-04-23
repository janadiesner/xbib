
package net.fortuna.ical4j.filter;

import java.util.Iterator;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;

/**
 * A rule that matches any component containing the specified property. Note that this rule ignores any parameters
 * matching only on the value of the property.
 */
public class HasPropertyRule extends ComponentRule {

    private Property property;

    private boolean matchEquals;

    /**
     * Constructs a new instance with the specified property. Ignores any parameters matching only on the value of the
     * property.
     * @param property a property instance to check for
     */
    public HasPropertyRule(final Property property) {
        this(property, false);
    }

    /**
     * Constructs a new instance with the specified property.
     * @param property the property to match
     * @param matchEquals if true, matches must contain an identical property (as indicated by
     * <code>Property.equals()</code>
     */
    public HasPropertyRule(final Property property, final boolean matchEquals) {
        this.property = property;
        this.matchEquals = matchEquals;
    }

    public final boolean match(final Component component) {
        boolean match = false;
        final PropertyList properties = component.getProperties(property.getName());
        for (final Iterator i = properties.iterator(); i.hasNext();) {
            final Property p = (Property) i.next();
            if (matchEquals && property.equals(p)) {
                match = true;
            }
            else if (property.getValue().equals(p.getValue())) {
                match = true;
            }
        }
        return match;
    }
}
