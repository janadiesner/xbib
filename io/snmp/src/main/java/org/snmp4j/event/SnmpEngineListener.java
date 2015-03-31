package org.snmp4j.event;

import java.util.EventListener;

/**
 * The <code>SnmpEngineListener</code> interface can be implemented by classes
 * that need to be informed about changes to the SNMP engine ID cache.
 */
public interface SnmpEngineListener extends EventListener {

    /**
     * An SNMP engine has been added to or removed from the engine cache.
     *
     * @param engineEvent the SnmpEngineEvent object describing the engine that has been added.
     */
    void engineChanged(SnmpEngineEvent engineEvent);

}
