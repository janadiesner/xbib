
package net.fortuna.ical4j.model.component;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.Validator;
import net.fortuna.ical4j.model.property.Method;

/**
 * Base class for components that may be added to a calendar.
 */
public abstract class CalendarComponent extends Component {

    /**
     * Validator instance that does nothing.
     */
    protected static final Validator EMPTY_VALIDATOR = new EmptyValidator();
    
    /**
     * @param name component name
     */
    public CalendarComponent(final String name) {
        super(name);
    }

    /**
     * @param name component name
     * @param properties component properties
     */
    public CalendarComponent(final String name, final PropertyList properties) {
        super(name, properties);
    }

    /**
     * Performs method-specific ITIP validation.
     * @param method the applicable method
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     */
    public final void validate(Method method) throws ValidationException {
        final Validator validator = getValidator(method);
        if (validator != null) {
            validator.validate();
        }
        else {
            throw new ValidationException("Unsupported method: " + method);
        }
    }

    /**
     * @param method a method to validate on
     * @return a validator for the specified method or null if the method is not supported
     */
    protected abstract Validator getValidator(Method method);
    
    /**
     * Apply validation for METHOD=PUBLISH.
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     * @deprecated
     */
    public final void validatePublish() throws ValidationException {
        validate(Method.PUBLISH);
    }

    /**
     * Apply validation for METHOD=REQUEST.
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     * @deprecated
     */
    public final void validateRequest() throws ValidationException {
        validate(Method.REQUEST);
    }

    /**
     * Apply validation for METHOD=REPLY.
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     * @deprecated
     */
    public final void validateReply() throws ValidationException {
        validate(Method.REPLY);
    }

    /**
     * Apply validation for METHOD=ADD.
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     * @deprecated
     */
    public final void validateAdd() throws ValidationException {
        validate(Method.ADD);
    }

    /**
     * Apply validation for METHOD=CANCEL.
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     * @deprecated
     */
    public final void validateCancel() throws ValidationException {
        validate(Method.CANCEL);
    }

    /**
     * Apply validation for METHOD=REFRESH.
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     * @deprecated
     */
    public final void validateRefresh() throws ValidationException {
        validate(Method.REFRESH);
    }

    /**
     * Apply validation for METHOD=COUNTER.
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     * @deprecated
     */
    public final void validateCounter() throws ValidationException {
        validate(Method.COUNTER);
    }

    /**
     * Apply validation for METHOD=DECLINE-COUNTER.
     * @throws net.fortuna.ical4j.model.ValidationException where the component does not comply with RFC2446
     * @deprecated
     */
    public final void validateDeclineCounter() throws ValidationException {
        validate(Method.DECLINE_COUNTER);
    }
    
    private static class EmptyValidator implements Validator {
        
		private static final long serialVersionUID = 1L;

        public void validate() throws ValidationException {

        }
    }
}
