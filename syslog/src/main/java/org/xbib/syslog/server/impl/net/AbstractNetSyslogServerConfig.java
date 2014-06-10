package org.xbib.syslog.server.impl.net;

import org.xbib.syslog.server.impl.AbstractSyslogServerConfig;

/**
 * AbstractNetSyslogServerConfig provides a base abstract implementation of the AbstractSyslogServerConfig
 * configuration interface.
 */
public abstract class AbstractNetSyslogServerConfig extends AbstractSyslogServerConfig {

    protected String host = null;
    protected int port = SYSLOG_PORT_DEFAULT;

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
