package org.xbib.syslog.impl.net;

import org.xbib.syslog.impl.AbstractSyslogConfig;

/**
 * AbstractNetSyslogConfig is an abstract extension of AbstractSyslogConfig
 * that provides configuration support for network-based syslog clients.
 */
public abstract class AbstractNetSyslogConfig extends AbstractSyslogConfig implements AbstractNetSyslogConfigIF {

    protected String host = SYSLOG_HOST_DEFAULT;
    protected int port = SYSLOG_PORT_DEFAULT;

    protected boolean cacheHostAddress = CACHE_HOST_ADDRESS_DEFAULT;

    protected int maxQueueSize = MAX_QUEUE_SIZE_DEFAULT;

    public AbstractNetSyslogConfig() {
        //
    }

    public AbstractNetSyslogConfig(int facility) {
        this.facility = facility;
    }

    public AbstractNetSyslogConfig(int facility, String host) {
        this.facility = facility;
        this.host = host;
    }

    public AbstractNetSyslogConfig(String host) {
        this.host = host;
    }

    public AbstractNetSyslogConfig(int facility, String host, int port) {
        this.facility = facility;
        this.host = host;
        this.port = port;
    }

    public AbstractNetSyslogConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean isCacheHostAddress() {
        return this.cacheHostAddress;
    }

    public void setCacheHostAddress(boolean cacheHostAddress) {
        this.cacheHostAddress = cacheHostAddress;
    }

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

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }
}
