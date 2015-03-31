package org.snmp4j.smi;

/**
 * The <code>VariantVariableCallback</code> can be implemented by
 * objects that want to intercept/monitor reading and writing of
 * a <code>VariantVariable</code>'s value.
 */
public interface VariantVariableCallback {

    /**
     * The supplied variable's value has been updated.
     *
     * @param variable the <code>VariantVariable</code> that has been updated.
     */
    void variableUpdated(VariantVariable variable);

    /**
     * The supplied variable needs to be updated because it is about
     * to be read.
     *
     * @param variable the <code>VariantVariable</code> that will be read.
     */
    void updateVariable(VariantVariable variable);

}
