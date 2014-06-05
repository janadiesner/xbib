package org.xbib.syslog.server;

import org.xbib.syslog.SyslogCharSetIF;
import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.SyslogRuntimeException;

import java.util.List;

/**
 * SyslogServerConfigIF provides a common, extensible configuration interface for all
 * implementations of SyslogServerIF.
 */
public interface SyslogServerConfigIF extends SyslogConstants, SyslogCharSetIF {

    public Class getSyslogServerClass();

    public String getHost();

    public void setHost(String host) throws SyslogRuntimeException;

    public int getPort();

    public void setPort(int port) throws SyslogRuntimeException;

    public boolean isUseDaemonThread();

    public void setUseDaemonThread(boolean useDaemonThread);

    public int getThreadPriority();

    public void setThreadPriority(int threadPriority);

    public List getEventHandlers();

    public long getShutdownWait();

    public void setShutdownWait(long shutdownWait);

    public void addEventHandler(SyslogServerEventHandlerIF eventHandler);

    public void insertEventHandler(int pos, SyslogServerEventHandlerIF eventHandler);

    public void removeEventHandler(SyslogServerEventHandlerIF eventHandler);

    public void removeAllEventHandlers();

    public boolean isUseStructuredData();

    public void setUseStructuredData(boolean useStructuredData);

    public Object getDateTimeFormatter();

    public void setDateTimeFormatter(Object dateTimeFormatter);
}
