package org.snmp4j.smi;

/**
 * The <code>Address</code> interface serves as a base class for all SNMP
 * transport addresses.
 * <p>
 * Note: This class should be moved to package <code>org.snmp4j</code>
 * in SNMP4J 2.0.
 * </p>
 */
public interface Address extends AssignableFromString, AssignableFromByteArray {

    /**
     * Checks whether this <code>Address</code> is a valid transport address.
     *
     * @return <code>true</code> if the address is valid, <code>false</code> otherwise.
     */
    boolean isValid();

    /**
     * Parses the address from the supplied string representation.
     *
     * @param address a String representation of this address.
     * @return <code>true</code> if <code>address</code> could be successfully
     * parsed and has been assigned to this address object, <code>false</code>
     * otherwise.
     */
    boolean parseAddress(String address);

    /**
     * Sets the address value from the supplied String. The string must match
     * the format required for the Address instance implementing this interface.
     * Otherwise an {@link IllegalArgumentException} runtime exception is thrown.
     *
     * @param address an address String.
     */
    void setValue(String address);
}

