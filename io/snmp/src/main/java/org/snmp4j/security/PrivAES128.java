package org.snmp4j.security;

import org.snmp4j.smi.OID;

/**
 * Encryption class for AES 128.
 */
public class PrivAES128 extends PrivAES {

    /**
     * Unique ID of this privacy protocol.
     */
    public static final OID ID = new OID("1.3.6.1.6.3.10.1.2.4");

    /**
     * Constructor.
     */
    public PrivAES128() {
        super(16);
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
