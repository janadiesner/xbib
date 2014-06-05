package org.xbib.syslog.impl.net.tcp;

import org.xbib.syslog.impl.net.AbstractNetSyslogConfigIF;

/**
 * TCPNetSyslogConfigIF is a configuration interface supporting TCP/IP-based
 * Syslog implementations.
 */
public interface TCPNetSyslogConfigIF extends AbstractNetSyslogConfigIF {

    public boolean isPersistentConnection();

    public void setPersistentConnection(boolean persistentConnection);

    public boolean isSoLinger();

    public void setSoLinger(boolean soLinger);

    public int getSoLingerSeconds();

    public void setSoLingerSeconds(int soLingerSeconds);

    public boolean isKeepAlive();

    public void setKeepAlive(boolean keepAlive);

    public boolean isReuseAddress();

    public void setReuseAddress(boolean reuseAddress);

    public boolean isSetBufferSize();

    public void setSetBufferSize(boolean setBufferSize);

    public int getFreshConnectionInterval();

    public void setFreshConnectionInterval(int interval);
}
