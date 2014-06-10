package org.xbib.syslog.server.impl.net.tcp;

import org.xbib.syslog.server.impl.net.AbstractNetSyslogServerConfig;

/**
 * TCPNetSyslogServerConfig provides configuration for TCPNetSyslogServer.
 */
public class TCPNetSyslogServerConfig extends AbstractNetSyslogServerConfig implements TCPNetSyslogServerConfigIF {

    protected int timeout = 0;
    protected int backlog = 0;
    protected int maxActiveSockets = TCP_MAX_ACTIVE_SOCKETS_DEFAULT;
    protected byte maxActiveSocketsBehavior = TCP_MAX_ACTIVE_SOCKETS_BEHAVIOR_DEFAULT;

    public TCPNetSyslogServerConfig() {
        //
    }

    public TCPNetSyslogServerConfig(int port) {
        this.port = port;
    }

    public TCPNetSyslogServerConfig(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    public TCPNetSyslogServerConfig(String host) {
        this.host = host;
    }

    public TCPNetSyslogServerConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public TCPNetSyslogServerConfig(String host, int port, int backlog) {
        this.host = host;
        this.port = port;
        this.backlog = backlog;
    }

    public Class getSyslogServerClass() {
        return TCPNetSyslogServer.class;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getBacklog() {
        return this.backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getMaxActiveSockets() {
        return maxActiveSockets;
    }

    public void setMaxActiveSockets(int maxActiveSockets) {
        this.maxActiveSockets = maxActiveSockets;
    }

    public byte getMaxActiveSocketsBehavior() {
        return maxActiveSocketsBehavior;
    }

    public void setMaxActiveSocketsBehavior(byte maxActiveSocketsBehavior) {
        this.maxActiveSocketsBehavior = maxActiveSocketsBehavior;
    }
}
