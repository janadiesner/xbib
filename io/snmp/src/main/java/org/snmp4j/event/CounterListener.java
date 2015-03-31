package org.snmp4j.event;

import org.snmp4j.security.SecurityModel;

import java.util.EventListener;

/**
 * The <code>CounterListener</code> interface has to be implemented by listener
 * for {@link org.snmp4j.event.CounterEvent} events. By implementing this method, an object is
 * able to be informed by a {@link org.snmp4j.mp.MessageProcessingModel},
 * {@link SecurityModel}, or other objects about conditions causing
 * certain counters to be incremented.
 *
 */
public interface CounterListener extends EventListener {

    /**
     * Increment the supplied counter instance and return the current value
     * (after incrementation) in the event object if the event receiver is the
     * maintainer of the counter value.
     *
     * @param event a <code>CounterEvent</code> instance.
     */
    void incrementCounter(CounterEvent event);

}
