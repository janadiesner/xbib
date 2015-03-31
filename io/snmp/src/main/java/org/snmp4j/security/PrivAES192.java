package org.snmp4j.security;

import org.snmp4j.smi.OID;

/**
 * Encryption class for AES 192.
 */
public class PrivAES192 extends PrivAES {

    /**
     * Unique ID of this privacy protocol.
     */
    public static final OID ID = new OID("1.3.6.1.4.1.4976.2.2.1.1.1");

    /**
     * Constructor.
     */
    public PrivAES192() {
        super(24);
    }

    /**
     * Gets the OID uniquely identifying the privacy protocol.
     *
     * @return an <code>OID</code> instance.
     */
    public OID getID() {
        return (OID) ID.clone();
    }

}
