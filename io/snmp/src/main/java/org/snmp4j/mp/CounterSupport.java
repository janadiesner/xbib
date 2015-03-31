package org.snmp4j.mp;

import org.snmp4j.event.CounterEvent;
import org.snmp4j.event.CounterListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>CounterSupport</code> class provides support to fire
 * {@link CounterEvent} to registered listeners.
 */
public class CounterSupport {

    protected static CounterSupport instance = null;
    private transient List<CounterListener> counterListeners;

    protected CounterSupport() {
    }

    /**
     * Gets the counter support singleton.
     *
     * @return the <code>CounterSupport</code> instance.
     */
    public static CounterSupport getInstance() {
        if (instance == null) {
            instance = new CounterSupport();
        }
        return instance;
    }

    /**
     * Adds a <code>CounterListener</code>.
     *
     * @param listener a <code>CounterListener</code> instance that needs to be informed when
     *                 a counter needs to be incremented.
     */
    public synchronized void addCounterListener(CounterListener listener) {
        if (counterListeners == null) {
            counterListeners = new ArrayList<CounterListener>(2);
        }
        if (!counterListeners.contains(listener)) {
            counterListeners.add(listener);
        }
    }

    /**
     * Removes a previously added <code>CounterListener</code>.
     *
     * @param listener a <code>CounterListener</code> instance.
     */
    public synchronized void removeCounterListener(CounterListener listener) {
        if (counterListeners != null && counterListeners.contains(listener)) {
            counterListeners.remove(listener);
        }
    }

    /**
     * Inform all registered listeners that the supplied counter needs to be
     * incremented.
     *
     * @param event a <code>CounterEvent</code> containing information about the counter to
     *              be incremented.
     */
    public void fireIncrementCounter(CounterEvent event) {
        if (counterListeners != null) {
            for (CounterListener l : counterListeners) {
                l.incrementCounter(event);
            }
        }
    }
}
