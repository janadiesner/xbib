
package net.fortuna.ical4j.filter;

import java.util.Date;

import net.fortuna.ical4j.model.DateRange;

/**
 */
public class DateInRangeRule extends AbstractDateRule {

    private final DateRange range;
    
    private final int inclusiveMask;
    
    /**
     * @param range the range to check
     * @param inclusiveMask indicates inclusiveness of start and end of the range
     */
    public DateInRangeRule(DateRange range, int inclusiveMask) {
        this.range = range;
        this.inclusiveMask = inclusiveMask;
    }

    protected boolean match(Date date) {
        return range.includes(date, inclusiveMask);
    }

}
