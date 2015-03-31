package org.snmp4j.mp;

/**
 * The <code>MutableStateReference</code> encapsulates a {@link org.snmp4j.mp.StateReference}
 * for read/write access.
 *
 */
public class MutableStateReference {

    private StateReference stateReference;

    public MutableStateReference() {
    }

    public StateReference getStateReference() {
        return stateReference;
    }

    public void setStateReference(StateReference stateReference) {
        this.stateReference = stateReference;
    }

}
