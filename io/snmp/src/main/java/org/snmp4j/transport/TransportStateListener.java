package org.snmp4j.transport;

import java.util.EventListener;

/**
 * The <code>TransportStateListener</code> interface can be implemented
 * to monitor the connection state for connection oriented transport mappings.
 */
public interface TransportStateListener extends EventListener {

    /**
     * The connection state of a transport protocol connection has been changed.
     *
     * @param change a <code>TransportStateEvent</code> instance.
     */
    void connectionStateChanged(TransportStateEvent change);
}
