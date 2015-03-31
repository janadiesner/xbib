package org.snmp4j.util;

import org.snmp4j.PDU;
import org.snmp4j.smi.VariableBinding;

/**
 * The <code>TreeEvent</code> class reports events in a tree retrieval
 * operation.
 */
public class TreeEvent extends RetrievalEvent {

    public TreeEvent(TreeUtils.TreeRequest source, Object userObject, VariableBinding[] vbs) {
        super(source, userObject, vbs);
    }

    public TreeEvent(TreeUtils.TreeRequest source, Object userObject, int status) {
        super(source, userObject, status);
    }

    public TreeEvent(TreeUtils.TreeRequest source, Object userObject, PDU report) {
        super(source, userObject, report);
    }

    public TreeEvent(TreeUtils.TreeRequest source, Object userObject, Exception exception) {
        super(source, userObject, exception);
    }

    /**
     * Gets the variable bindings retrieved in depth first order from the
     * (sub-)tree.
     *
     * @return VariableBinding[]
     * a possibly empty or <code>null</code> array of
     * <code>VariableBindings</code>.
     */
    public VariableBinding[] getVariableBindings() {
        return vbs;
    }

}
