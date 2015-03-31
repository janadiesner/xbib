package org.snmp4j.security;

import org.snmp4j.smi.OID;

/**
 * The <code>SecurityProtocol</code> class defines common methods of
 * authentication and privacy protocols.
 */
public interface SecurityProtocol {

    /**
     * Gets the OID uniquely identifying the privacy protocol.
     *
     * @return an <code>OID</code> instance.
     */
    OID getID();

}

