
package net.fortuna.ical4j.util;

import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.ValidationException;

/**
 * @author Ben
 *
 */
public final class ComponentValidator {

    private static final String ASSERT_NONE_MESSAGE = "Component [{0}] is not applicable";

    private static final String ASSERT_ONE_OR_LESS_MESSAGE = "Component [{0}] must only be specified once";
    
    /**
     * Constructor made private to enforce static nature.
     */
    private ComponentValidator() {
    }
    
    /**
     * @param componentName a component name used in the assertion
     * @param components a list of components
     * @throws net.fortuna.ical4j.model.ValidationException where the assertion fails
     */
    public static void assertNone(String componentName, ComponentList components) throws ValidationException {
        if (components.getComponent(componentName) != null) {
            throw new ValidationException(ASSERT_NONE_MESSAGE, new Object[] {componentName});
        }
    }
    
    /**
     * @param componentName a component name used in the assertion
     * @param components a list of components
     * @throws net.fortuna.ical4j.model.ValidationException where the assertion fails
     */
    public static void assertOneOrLess(String componentName, ComponentList components) throws ValidationException {
        if (components.getComponents(componentName).size() > 1) {
            throw new ValidationException(ASSERT_ONE_OR_LESS_MESSAGE, new Object[] {componentName});
        }
    }
}
