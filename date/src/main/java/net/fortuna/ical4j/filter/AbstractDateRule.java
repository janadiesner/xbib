
package net.fortuna.ical4j.filter;

import java.util.Date;

public abstract class AbstractDateRule implements Rule {

    public boolean match(Object o) {
        return match((Date) o);
    }

    /**
     * @param date the date to check
     * @return true if the date matches rule requirements, otherwise false
     */
    protected abstract boolean match(Date date);
}
