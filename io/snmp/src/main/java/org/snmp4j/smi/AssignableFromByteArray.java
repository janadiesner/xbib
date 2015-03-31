package org.snmp4j.smi;

/**
 * The <code>AssignableFromByteArray</code> interface describes objects whose
 * value can be set from a byte array and converted back to a byte array.
 */
public interface AssignableFromByteArray {

    /**
     * Sets the value of this object from the supplied byte array.
     *
     * @param value a byte array.
     */
    void setValue(byte[] value);

    /**
     * Returns the value of this object as a byte array.
     *
     * @return a byte array.
     */
    byte[] toByteArray();
}
