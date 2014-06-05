package org.xbib.syslog.server.impl.net.tcp;

import org.xbib.syslog.server.SyslogServerConfigIF;

/**
 * TCPNetSyslogServerConfigIF provides configuration for TCPNetSyslogServer.
 */
public interface TCPNetSyslogServerConfigIF extends SyslogServerConfigIF {

    public final static byte MAX_ACTIVE_SOCKETS_BEHAVIOR_BLOCK = 0;

    public final static byte MAX_ACTIVE_SOCKETS_BEHAVIOR_REJECT = 1;

    public int getTimeout();

    public void setTimeout(int timeout);

    public int getBacklog();

    public void setBacklog(int backlog);

    public int getMaxActiveSockets();

    public void setMaxActiveSockets(int maxActiveSockets);

    public byte getMaxActiveSocketsBehavior();

    public void setMaxActiveSocketsBehavior(byte maxActiveSocketsBehavior);
}
