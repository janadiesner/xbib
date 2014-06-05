package org.xbib.syslog.impl.net;

import org.xbib.syslog.impl.AbstractSyslogConfigIF;

/**
 * AbstractNetSyslogConfigIF is a configuration interface supporting network-based
 * Syslog implementations.
 */
public interface AbstractNetSyslogConfigIF extends AbstractSyslogConfigIF {
    public boolean isCacheHostAddress();

    public void setCacheHostAddress(boolean cacheHostAddress);
}
