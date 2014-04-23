
package net.fortuna.ical4j.filter;

/**
 * Implementors dictate whether a given object is classified as a "match" for the enclosing filter.
 */
public interface Rule {

    /**
     * Indicates whether the specified object passes the conditions required to match this rule.
     * @param o an object to match
     * @return boolean indicated if the specified object matches this rule
     */
    boolean match(Object o);
}
