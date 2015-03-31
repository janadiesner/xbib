package org.snmp4j.event;

import java.util.EventListener;

/**
 * The <code>AuthenticationFailureListener</code> listens for authentication
 * failure events.
 */
public interface AuthenticationFailureListener extends EventListener {

    /**
     * Informs about an authentication failure occurred while processing the
     * message contained in the supplied event object.
     *
     * @param event a <code>AuthenticationFailureEvent</code> describing the type of
     *              authentication failure, the offending message, and its source address
     *              and transport protocol.
     */
    void authenticationFailure(AuthenticationFailureEvent event);
}
