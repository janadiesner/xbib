package org.xbib.syslog.server;

public abstract interface SyslogServerEventHandlerIF {
    public void initialize(SyslogServerIF syslogServer);

    public void destroy(SyslogServerIF syslogServer);
}
