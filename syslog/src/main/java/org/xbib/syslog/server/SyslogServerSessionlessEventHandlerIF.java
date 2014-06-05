package org.xbib.syslog.server;

import java.net.SocketAddress;


/**
 * SyslogServerEventHandlerIF provides an extensible interface for
 * server event handlers.
 */
public interface SyslogServerSessionlessEventHandlerIF extends SyslogServerEventHandlerIF {

    public void event(SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event);

    public void exception(SyslogServerIF syslogServer, SocketAddress socketAddress, Exception exception);
}
