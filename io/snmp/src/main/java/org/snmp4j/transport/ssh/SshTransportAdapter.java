package org.snmp4j.transport.ssh;

import org.snmp4j.TransportStateReference;

/**
 * The <code>SshTransportAdapter</code> adapts 3rd party SSH transport protocol
 * implementations for SNMP4J.
 */
public interface SshTransportAdapter<I> {

    SshSession<I> openClientSession(TransportStateReference tmStateReference, int maxMessageSize);

    SshSession<I> openServerSession(TransportStateReference tmStateReference, int maxMessageSize);

    boolean closeSession(Long sessionID);

}
