
package net.fortuna.ical4j.filter;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;

/**
 * A rule that matches any component that occurs in the specified time period.
 */
public class PeriodRule extends ComponentRule {

    private Period period;

    /**
     * Constructs a new instance using the specified period.
     * @param period a period instance to match on
     */
    public PeriodRule(final Period period) {
        this.period = period;
    }

    public final boolean match(final Component component) {

        final PeriodList recurrenceSet = component.calculateRecurrenceSet(period);
        return (!recurrenceSet.isEmpty());
    }
}
