package org.snmp4j.security;

import org.snmp4j.smi.OID;

/**
 * The AuthMD5 class implements the MD5 authentication protocol.
 *
 */
public class AuthMD5 extends AuthGeneric {

    public static final OID ID = new OID("1.3.6.1.6.3.10.1.1.2");

    public AuthMD5() {
        super("MD5", 16);
    }

    public OID getID() {
        return (OID) ID.clone();
    }
}
