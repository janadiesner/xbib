package org.snmp4j.transport;

import org.snmp4j.MessageDispatcher;
import org.snmp4j.TransportMapping;
import org.snmp4j.TransportStateReference;
import org.snmp4j.smi.Address;

import java.nio.ByteBuffer;

/**
 * The <code>TransportListener</code> interface is implemented by objects
 * that process incoming messages from <code>TransportMapping</code>s, for
 * example {@link MessageDispatcher}.
 *
 */
public interface TransportListener {

    /**
     * Processes an incoming message.
     *
     * @param sourceTransport  a <code>TransportMapping</code> instance denoting the transport that
     *                         received the message and that will be used to send any responses to
     *                         this message. The <code>sourceTransport</code> has to support the
     *                         <code>incomingAddress</code>'s implementation class.
     * @param incomingAddress  the <code>Address</code> from which the message has been received.
     * @param wholeMessage     an <code>ByteBuffer</code> containing the received message.
     * @param tmStateReference the transport model state reference as defined by RFC 5590
     */
    void processMessage(TransportMapping sourceTransport,
                        Address incomingAddress,
                        ByteBuffer wholeMessage,
                        TransportStateReference tmStateReference);
}
