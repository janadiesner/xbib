package org.snmp4j.transport;

import org.snmp4j.smi.Address;

import java.io.IOException;
import java.util.EventObject;

/**
 * The <code>TransportStateEvent</code> describes a state change for
 * a transport connection. Optionally, connection establishment can be
 * cancelled.
 *
 */
public class TransportStateEvent extends EventObject {

    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_DISCONNECTED_REMOTELY = 2;
    public static final int STATE_DISCONNECTED_TIMEOUT = 3;
    public static final int STATE_CLOSED = 4;
    private int newState;
    private Address peerAddress;
    private IOException causingException;

    private boolean cancelled = false;

    public TransportStateEvent(TcpTransportMapping source,
                               Address peerAddress,
                               int newState,
                               IOException causingException) {
        super(source);
        this.newState = newState;
        this.peerAddress = peerAddress;
        this.causingException = causingException;
    }

    public IOException getCausingException() {
        return causingException;
    }

    public int getNewState() {
        return newState;
    }

    public Address getPeerAddress() {
        return peerAddress;
    }

    /**
     * Indicates whether this event has been canceled. Only
     * {@link #STATE_CONNECTED} events can be canceled.
     *
     * @return <code>true</code> if the event has been canceled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the canceled state of the transport event. Only
     * {@link #STATE_CONNECTED} events can be canceled.
     *
     * @param cancelled <code>true</code> if the event should be canceled, i.e. a connection
     *                  attempt should be rejected.
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String toString() {
        return TransportStateEvent.class.getName() + "[source=" + source +
                ",peerAddress=" + peerAddress +
                ",newState=" + newState +
                ",cancelled=" + cancelled +
                ",causingException=" + causingException + "]";
    }
}
