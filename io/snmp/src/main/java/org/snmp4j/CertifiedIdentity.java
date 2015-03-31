package org.snmp4j;

import org.snmp4j.smi.OctetString;

/**
 * The <code>CertifiedIdentity</code>  interface describes an identity
 * that is associated with a client certificate fingerprint and a server
 * certificate fingerprint.
 */
public interface CertifiedIdentity {

    OctetString getServerFingerprint();

    OctetString getClientFingerprint();

    OctetString getIdentity();

}
