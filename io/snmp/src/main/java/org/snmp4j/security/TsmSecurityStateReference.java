package org.snmp4j.security;

import org.snmp4j.TransportStateReference;

/**
 * The <code>TsmSecurityStateReference</code> holds cached security data
 * for the {@link TSM} security model.
 */
public class TsmSecurityStateReference implements SecurityStateReference {

    private TransportStateReference tmStateReference;

    public TsmSecurityStateReference() {
    }

    public TransportStateReference getTmStateReference() {
        return tmStateReference;
    }

    public void setTmStateReference(TransportStateReference tmStateReference) {
        this.tmStateReference = tmStateReference;
    }

    @Override
    public String toString() {
        return "TsmSecurityStateReference[" +
                "tmStateReference=" + tmStateReference +
                ']';
    }
}
