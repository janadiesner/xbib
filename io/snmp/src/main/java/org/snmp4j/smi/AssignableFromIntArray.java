package org.snmp4j.smi;

/**
 * The <code>AssignableFromIntArray</code> interface describes objects whose
 * value can be set from an int array and converted back to an int array.
 */
public interface AssignableFromIntArray {

    /**
     * Sets the value of this object from the supplied int array.
     *
     * @param value an int array.
     */
    void setValue(int[] value);

    /**
     * Returns the value of this object as an int array.
     *
     * @return an int array.
     */
    int[] toIntArray();
}
