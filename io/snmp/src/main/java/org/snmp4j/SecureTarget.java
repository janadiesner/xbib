package org.snmp4j;

import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;

/**
 * The <code>SecureTarget</code> is an security model independent abstract class
 * for all targets supporting secure SNMP communication.
 */
public abstract class SecureTarget extends AbstractTarget {

    /**
     * Default constructor.
     */
    protected SecureTarget() {
    }

    /**
     * Creates a SNMPv3 secure target with an address and security name.
     *
     * @param address      an <code>Address</code> instance denoting the transport address of the
     *                     target.
     * @param securityName a <code>OctetString</code> instance representing the security name
     *                     of the USM user used to access the target.
     */
    protected SecureTarget(Address address, OctetString securityName) {
        super(address, securityName);
    }

    @Override
    public String toString() {
        return "SecureTarget[" + toStringAbstractTarget() + ']';
    }
}
