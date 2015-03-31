package org.snmp4j;

import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;

/**
 * User based target for SNMPv3 or later.
 */
public class UserTarget extends SecureTarget {


    private OctetString authoritativeEngineID = new OctetString();

    /**
     * Creates a target for a user based security model target.
     */
    public UserTarget() {
        setSecurityModel(MPv3.ID);
    }

    /**
     * Creates a SNMPv3 USM target with security level noAuthNoPriv, one second
     * time-out without retries.
     *
     * @param address               the transport <code>Address</code> of the target.
     * @param securityName          the USM security name to be used to access the target.
     * @param authoritativeEngineID the authoritative engine ID as a possibly zero length byte
     *                              array which must not be <code>null</code>.
     */
    public UserTarget(Address address, OctetString securityName,
                      byte[] authoritativeEngineID) {
        super(address, securityName);
        setAuthoritativeEngineID(authoritativeEngineID);
        setSecurityModel(MPv3.ID);
    }

    /**
     * Creates a SNMPv3 USM target with the supplied security level, one second
     * time-out without retries.
     *
     * @param address               the transport <code>Address</code> of the target.
     * @param securityName          the USM security name to be used to access the target.
     * @param authoritativeEngineID the authoritative engine ID as a possibly zero length byte
     *                              array which must not be <code>null</code>.
     * @param securityLevel         the {@link org.snmp4j.security.SecurityLevel} to use.
     */
    public UserTarget(Address address, OctetString securityName,
                      byte[] authoritativeEngineID, int securityLevel) {
        super(address, securityName);
        setAuthoritativeEngineID(authoritativeEngineID);
        setSecurityLevel(securityLevel);
        setSecurityModel(MPv3.ID);
    }

    /**
     * Gets the authoritative engine ID of this target.
     *
     * @return a possibly zero length byte array.
     */
    public byte[] getAuthoritativeEngineID() {
        return authoritativeEngineID.getValue();
    }

    /**
     * Sets the authoritative engine ID of this target.
     *
     * @param authoritativeEngineID a possibly zero length byte array (must not be <code>null</code>).
     */
    public void setAuthoritativeEngineID(byte[] authoritativeEngineID) {
        this.authoritativeEngineID.setValue(authoritativeEngineID);
    }

    @Override
    public String toString() {
        return "UserTarget[" + toStringAbstractTarget() +
                ", authoritativeEngineID=" + authoritativeEngineID +
                ']';
    }
}

