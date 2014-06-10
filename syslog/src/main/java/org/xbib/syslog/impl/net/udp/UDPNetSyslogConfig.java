package org.xbib.syslog.impl.net.udp;

import org.xbib.syslog.impl.net.AbstractNetSyslogConfig;

/**
 * UDPNetSyslogConfig is an extension of AbstractNetSyslogConfig that provides
 * configuration support for UDP/IP-based syslog clients.
 */
public class UDPNetSyslogConfig extends AbstractNetSyslogConfig {

    public UDPNetSyslogConfig() {
        super();
    }

    public UDPNetSyslogConfig(int facility, String host, int port) {
        super(facility, host, port);
    }

    public UDPNetSyslogConfig(int facility, String host) {
        super(facility, host);
    }

    public UDPNetSyslogConfig(int facility) {
        super(facility);
    }

    public UDPNetSyslogConfig(String host, int port) {
        super(host, port);
    }

    public UDPNetSyslogConfig(String host) {
        super(host);
    }

    public Class getSyslogClass() {
        return UDPNetSyslog.class;
    }
}
