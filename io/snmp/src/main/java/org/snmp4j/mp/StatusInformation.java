package org.snmp4j.mp;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.VariableBinding;

/**
 * The <code>StatusInformation</code> class represents status information
 * of a SNMPv3 message that is needed to return a report message.
 */
public class StatusInformation {

    private VariableBinding errorIndication;
    private byte[] contextName;
    private byte[] contextEngineID;
    private Integer32 securityLevel;

    public StatusInformation() {
    }

    public StatusInformation(VariableBinding errorIndication,
                             byte[] contextName,
                             byte[] contextEngineID,
                             Integer32 securityLevel) {
        this.errorIndication = errorIndication;
        this.contextName = contextName;
        this.contextEngineID = contextEngineID;
        this.securityLevel = securityLevel;
    }

    public VariableBinding getErrorIndication() {
        return errorIndication;
    }

    public void setErrorIndication(VariableBinding errorIndication) {
        this.errorIndication = errorIndication;
    }

    public byte[] getContextName() {
        return contextName;
    }

    public void setContextName(byte[] contextName) {
        this.contextName = contextName;
    }

    public byte[] getContextEngineID() {
        return contextEngineID;
    }

    public void setContextEngineID(byte[] contextEngineID) {
        this.contextEngineID = contextEngineID;
    }

    public org.snmp4j.smi.Integer32 getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(org.snmp4j.smi.Integer32 securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String toString() {
        if (errorIndication == null) {
            return "noError";
        }
        return errorIndication.toString();
    }
}

