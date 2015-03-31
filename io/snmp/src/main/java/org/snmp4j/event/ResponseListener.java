package org.snmp4j.event;

import java.util.EventListener;

/**
 * The <code>ResponseListener</code> interface is implemented by objects that
 * process SNMP response messages.
 */
public interface ResponseListener extends EventListener {

    /**
     * Process a SNMP response.
     *
     * @param event a <code>ResponseEvent</code>.
     */
    void onResponse(ResponseEvent event);

}
