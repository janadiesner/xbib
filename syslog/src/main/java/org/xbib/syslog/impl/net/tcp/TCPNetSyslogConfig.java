package org.xbib.syslog.impl.net.tcp;

import org.xbib.syslog.impl.net.AbstractNetSyslogConfig;

/**
 * TCPNetSyslogConfig is an extension of AbstractNetSyslogConfig that provides
 * configuration support for TCP/IP-based syslog clients.
 */
public class TCPNetSyslogConfig extends AbstractNetSyslogConfig implements TCPNetSyslogConfigIF {

    public static byte SYSTEM_DELIMITER_SEQUENCE = (byte)'\n';

    protected byte delimiterSequence = SYSTEM_DELIMITER_SEQUENCE;

    protected boolean persistentConnection = TCP_PERSISTENT_CONNECTION_DEFAULT;

    protected boolean soLinger = TCP_SO_LINGER_DEFAULT;
    protected int soLingerSeconds = TCP_SO_LINGER_SECONDS_DEFAULT;

    protected boolean keepAlive = TCP_KEEP_ALIVE_DEFAULT;

    protected boolean reuseAddress = TCP_REUSE_ADDRESS_DEFAULT;

    protected boolean setBufferSize = TCP_SET_BUFFER_SIZE_DEFAULT;

    protected int freshConnectionInterval = TCP_FRESH_CONNECTION_INTERVAL_DEFAULT;

    public TCPNetSyslogConfig() {
        initialize();
    }

    protected void initialize() {
        //
    }

    public TCPNetSyslogConfig(int facility, String host, int port) {
        super(facility, host, port);
        initialize();
    }

    public TCPNetSyslogConfig(int facility, String host) {
        super(facility, host);
        initialize();
    }

    public TCPNetSyslogConfig(int facility) {
        super(facility);
        initialize();
    }

    public TCPNetSyslogConfig(String host, int port) {
        super(host, port);
        initialize();
    }

    public TCPNetSyslogConfig(String host) {
        super(host);
        initialize();
    }

    public Class getSyslogClass() {
        return TCPNetSyslog.class;
    }

    public boolean isPersistentConnection() {
        return this.persistentConnection;
    }

    public void setPersistentConnection(boolean persistentConnection) {
        this.persistentConnection = persistentConnection;
    }

    public boolean isSoLinger() {
        return this.soLinger;
    }

    public void setSoLinger(boolean soLinger) {
        this.soLinger = soLinger;
    }

    public int getSoLingerSeconds() {
        return this.soLingerSeconds;
    }

    public void setSoLingerSeconds(int soLingerSeconds) {
        this.soLingerSeconds = soLingerSeconds;
    }

    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isReuseAddress() {
        return this.reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public boolean isSetBufferSize() {
        return this.setBufferSize;
    }

    public void setSetBufferSize(boolean setBufferSize) {
        this.setBufferSize = setBufferSize;
    }

    public int getFreshConnectionInterval() {
        return freshConnectionInterval;
    }

    public void setFreshConnectionInterval(int freshConnectionInterval) {
        this.freshConnectionInterval = freshConnectionInterval;
    }

    public Class getSyslogWriterClass() {
        return TCPNetSyslogWriter.class;
    }
}
