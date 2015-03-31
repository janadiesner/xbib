package org.snmp4j;

import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TlsAddress;

/**
 * The <code>CertifiedTarget</code> class implements a {@link org.snmp4j.SecureTarget}
 * for usage with {@link org.snmp4j.security.SecurityModel}s that support
 * secured connections using client and server certificates.
 *
 */
public class CertifiedTarget extends SecureTarget implements CertifiedIdentity {

    private OctetString serverFingerprint;
    private OctetString clientFingerprint;

    public CertifiedTarget(OctetString identity) {
        super(new TlsAddress(), identity);
    }

    public CertifiedTarget(Address address, OctetString identity,
                           OctetString serverFingerprint, OctetString clientFingerprint) {
        super(address, identity);
        this.serverFingerprint = serverFingerprint;
        this.clientFingerprint = clientFingerprint;
    }

    public OctetString getServerFingerprint() {
        return serverFingerprint;
    }

    public OctetString getClientFingerprint() {
        return clientFingerprint;
    }

    public OctetString getIdentity() {
        return super.getSecurityName();
    }

    @Override
    public String toString() {
        return "CertifiedTarget[" + toStringAbstractTarget() +
                ", serverFingerprint=" + serverFingerprint +
                ", clientFingerprint=" + clientFingerprint +
                ']';
    }
}
