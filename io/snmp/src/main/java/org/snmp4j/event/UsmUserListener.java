package org.snmp4j.event;

import java.util.EventListener;

/**
 * The <code>UsmUserListener</code> interface is implemented by objects that
 * need to be informed when a USM user is created, modified, or deleted.
 */
public interface UsmUserListener extends EventListener {

    /**
     * Indicates a USM user change.
     *
     * @param event an <code>UsmUserEvent</code>.
     */
    void usmUserChange(UsmUserEvent event);

}
