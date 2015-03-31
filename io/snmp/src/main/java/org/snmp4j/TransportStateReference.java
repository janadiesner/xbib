package org.snmp4j;

import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;

/**
 * The <code>TransportStateReference</code> class holds information defined by
 * RFC 5343 for the tmStateReference ASI elements. Objects of this
 * class are cached by security aware {@link org.snmp4j.TransportMapping}s and
 * transport aware {@link org.snmp4j.security.SecurityModel}s.
 *
 */
public class TransportStateReference {

    private TransportMapping transport;
    private Address address;
    private OctetString securityName;
    private SecurityLevel requestedSecurityLevel;
    private SecurityLevel transportSecurityLevel;
    private boolean sameSecurity;
    private Object sessionID;
    private CertifiedIdentity certifiedIdentity;

    public TransportStateReference(TransportMapping transport,
                                   Address address,
                                   OctetString securityName,
                                   SecurityLevel requestedSecurityLevel,
                                   SecurityLevel transportSecurityLevel,
                                   boolean sameSecurity,
                                   Object sessionID) {
        this.transport = transport;
        this.address = address;
        this.securityName = securityName;
        this.requestedSecurityLevel = requestedSecurityLevel;
        this.transportSecurityLevel = transportSecurityLevel;
        this.sameSecurity = sameSecurity;
        this.sessionID = sessionID;
    }

    public TransportStateReference(TransportMapping transport,
                                   Address address,
                                   OctetString securityName,
                                   SecurityLevel requestedSecurityLevel,
                                   SecurityLevel transportSecurityLevel,
                                   boolean sameSecurity,
                                   Object sessionID,
                                   CertifiedIdentity certifiedIdentity) {
        this(transport, address, securityName, requestedSecurityLevel, transportSecurityLevel,
                sameSecurity, sessionID);
        this.certifiedIdentity = certifiedIdentity;
    }

    public TransportMapping getTransport() {
        return transport;
    }

    public Address getAddress() {
        return address;
    }

    public OctetString getSecurityName() {
        return securityName;
    }

    public void setSecurityName(OctetString securityName) {
        this.securityName = securityName;
    }

    public SecurityLevel getRequestedSecurityLevel() {
        return requestedSecurityLevel;
    }

    public void setRequestedSecurityLevel(SecurityLevel requestedSecurityLevel) {
        this.requestedSecurityLevel = requestedSecurityLevel;
    }

    public SecurityLevel getTransportSecurityLevel() {
        return transportSecurityLevel;
    }

    public void setTransportSecurityLevel(SecurityLevel transportSecurityLevel) {
        this.transportSecurityLevel = transportSecurityLevel;
    }

    public boolean isSameSecurity() {
        return sameSecurity;
    }

    public void setSameSecurity(boolean sameSecurity) {
        this.sameSecurity = sameSecurity;
    }

    public Object getSessionID() {
        return sessionID;
    }

    public CertifiedIdentity getCertifiedIdentity() {
        return certifiedIdentity;
    }

    /**
     * Checks if transport, address, securityName and transportSecurityLevel
     * are valid (not null).
     *
     * @return <code>true</code> if the above fields are not <code>null</code>.
     */
    public boolean isTransportSecurityValid() {
        return ((transport != null) && (address != null) && (securityName != null) &&
                (transportSecurityLevel != null));
    }

    @Override
    public String toString() {
        return "TransportStateReference[" +
                "transport=" + transport +
                ", address=" + address +
                ", securityName=" + securityName +
                ", requestedSecurityLevel=" + requestedSecurityLevel +
                ", transportSecurityLevel=" + transportSecurityLevel +
                ", sameSecurity=" + sameSecurity +
                ", sessionID=" + sessionID +
                ", certifiedIdentity=" + certifiedIdentity +
                ']';
    }

}
