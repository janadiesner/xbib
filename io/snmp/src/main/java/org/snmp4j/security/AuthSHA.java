package org.snmp4j.security;

import org.snmp4j.smi.OID;

/**
 * The <code>SHA</code> class implements the Secure Hash Authentication.
 */
public class AuthSHA extends AuthGeneric {

    public static final OID ID = new OID("1.3.6.1.6.3.10.1.1.3");

    public AuthSHA() {
        super("SHA-1", 20);
    }

    public OID getID() {
        return (OID) ID.clone();
    }

}
