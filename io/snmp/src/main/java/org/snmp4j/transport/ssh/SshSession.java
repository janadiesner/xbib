package org.snmp4j.transport.ssh;

import org.snmp4j.TransportStateReference;
import org.snmp4j.transport.TransportListener;

/**
 * The <code>SshSession</code> interface provides access to a SSH session
 * provided by a {@link SshTransportAdapter}.
 */
public interface SshSession<I> {

    Long getID();

    TransportStateReference getTransportStateReference();

    void setTransportStateReference(TransportStateReference tmStateReference);

    I getImplementation();

    void addTransportListener(TransportListener transportListener);

    void removeTransportListener(TransportListener transportListener);
}
