package org.xbib.syslog.impl.net;

import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.AbstractSyslog;
import org.xbib.syslog.util.SyslogUtility;

import java.net.InetAddress;

/**
 * AbstractNetSyslog is an abstract extension of AbstractSyslog
 * that provides support for network-based syslog clients.
 */
public abstract class AbstractNetSyslog extends AbstractSyslog {

    protected static final Object cachedHostAddressSyncObject = new Object();

    protected InetAddress cachedHostAddress = null;

    protected AbstractNetSyslogConfigIF netSyslogConfig = null;

    protected void initialize() throws SyslogRuntimeException {
        try {
            this.netSyslogConfig = (AbstractNetSyslogConfigIF) this.syslogConfig;
        } catch (ClassCastException cce) {
            throw new SyslogRuntimeException("config must implement interface AbstractNetSyslogConfigIF");
        }
    }

    /**
     * @return Returns an object of InetAddress of the local host, using caching if so directed.
     */
    public InetAddress getHostAddress() {
        InetAddress hostAddress = null;
        if (this.netSyslogConfig.isCacheHostAddress()) {
            if (this.cachedHostAddress == null) {
                synchronized (cachedHostAddressSyncObject) {
                    if (this.cachedHostAddress == null) {
                        this.cachedHostAddress = SyslogUtility.getInetAddress(this.syslogConfig.getHost());
                    }
                }
            }
            hostAddress = this.cachedHostAddress;
        } else {
            hostAddress = SyslogUtility.getInetAddress(this.syslogConfig.getHost());
        }
        return hostAddress;
    }
}
