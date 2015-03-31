package org.snmp4j.util;

/**
 * The <code>TimerFactory</code> describes a factory for
 * <code>CommonTimer</code> instances.
 */
public interface TimerFactory {

    /**
     * Creates a new timer instance.
     *
     * @return a <code>Timer</code>.
     */
    CommonTimer createTimer();

}
