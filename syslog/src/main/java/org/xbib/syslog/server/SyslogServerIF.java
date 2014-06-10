package org.xbib.syslog.server;

import org.xbib.syslog.SyslogRuntimeException;

/**
 * SyslogServerIF provides a common interface for all server implementations.
 */
public interface SyslogServerIF extends Runnable {

    public void initialize(String protocol, SyslogServerConfigIF config) throws SyslogRuntimeException;

    public String getProtocol();

    public SyslogServerConfigIF getConfig();

    public void run();

    public Thread getThread();

    public void setThread(Thread thread);

    public void shutdown();
}
