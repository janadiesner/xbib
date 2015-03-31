package org.snmp4j.transport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.TransportStateReference;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.TcpAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The <code>TcpTransportMapping</code> is the abstract base class for
 * TCP transport mappings.
 */
public abstract class TcpTransportMapping
        extends AbstractTransportMapping<TcpAddress>
        implements ConnectionOrientedTransportMapping<TcpAddress> {

    private static final Logger logger = LogManager.getLogger(TcpTransportMapping.class);

    protected TcpAddress tcpAddress;
    private transient List<TransportStateListener> transportStateListeners;

    public TcpTransportMapping(TcpAddress tcpAddress) {
        this.tcpAddress = tcpAddress;
    }

    public Class<? extends Address> getSupportedAddressClass() {
        return TcpAddress.class;
    }

    /**
     * Returns the transport address that is used by this transport mapping for
     * sending and receiving messages.
     *
     * @return the <code>Address</code> used by this transport mapping. The returned
     * instance must not be modified!
     */
    public TcpAddress getAddress() {
        return tcpAddress;
    }

    public TcpAddress getListenAddress() {
        return tcpAddress;
    }

    public abstract void sendMessage(TcpAddress address, byte[] message,
                                     TransportStateReference tmStateReference)
            throws IOException;

    public abstract void listen() throws IOException;

    public abstract void close() throws IOException;

    /**
     * Returns the <code>MessageLengthDecoder</code> used by this transport
     * mapping.
     *
     * @return a MessageLengthDecoder instance.
     */
    public abstract MessageLengthDecoder getMessageLengthDecoder();

    /**
     * Sets the <code>MessageLengthDecoder</code> that decodes the total
     * message length from the header of a message.
     *
     * @param messageLengthDecoder a MessageLengthDecoder instance.
     */
    public abstract void
    setMessageLengthDecoder(MessageLengthDecoder messageLengthDecoder);

    /**
     * Sets the connection timeout. This timeout specifies the time a connection
     * may be idle before it is closed.
     *
     * @param connectionTimeout the idle timeout in milliseconds. A zero or negative value will disable
     *                          any timeout and connections opened by this transport mapping will stay
     *                          opened until they are explicitly closed.
     */
    public abstract void setConnectionTimeout(long connectionTimeout);

    public synchronized void addTransportStateListener(TransportStateListener l) {
        if (transportStateListeners == null) {
            transportStateListeners = new ArrayList<TransportStateListener>(2);
        }
        transportStateListeners.add(l);
    }

    public synchronized void removeTransportStateListener(TransportStateListener
                                                                  l) {
        if (transportStateListeners != null) {
            transportStateListeners.remove(l);
        }
    }

    protected void fireConnectionStateChanged(TransportStateEvent change) {
        if (logger.isDebugEnabled()) {
            logger.debug("Firing transport state event: " + change);
        }
        final List<TransportStateListener> listenersFinalRef = transportStateListeners;
        if (listenersFinalRef != null) {
            try {
                List<TransportStateListener> listeners;
                synchronized (listenersFinalRef) {
                    listeners = new ArrayList<TransportStateListener>(listenersFinalRef);
                }
                for (TransportStateListener listener : listeners) {
                    listener.connectionStateChanged(change);
                }
            } catch (RuntimeException ex) {
                logger.error("Exception in fireConnectionStateChanged: " + ex.getMessage(), ex);
                if (SNMP4JSettings.isForwardRuntimeExceptions()) {
                    throw ex;
                }
            }
        }
    }
}
