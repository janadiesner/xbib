package org.xbib.syslog.server.impl.net.udp;

import org.xbib.syslog.server.impl.net.AbstractNetSyslogServerConfig;

/**
 * UDPNetSyslogServerConfig provides configuration for UDPNetSyslogServer.
 */
public class UDPNetSyslogServerConfig extends AbstractNetSyslogServerConfig {

    public UDPNetSyslogServerConfig() {
        //
    }

    public UDPNetSyslogServerConfig(int port) {
        this.port = port;
    }

    public UDPNetSyslogServerConfig(String host) {
        this.host = host;
    }

    public UDPNetSyslogServerConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Class getSyslogServerClass() {
        return UDPNetSyslogServer.class;
    }
}
