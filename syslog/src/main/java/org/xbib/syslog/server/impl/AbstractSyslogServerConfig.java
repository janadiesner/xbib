package org.xbib.syslog.server.impl;

import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.server.SyslogServerConfigIF;
import org.xbib.syslog.server.SyslogServerEventHandlerIF;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractSyslogServerConfig provides a base abstract implementation of the SyslogServerConfigIF
 * configuration interface.
 */
public abstract class AbstractSyslogServerConfig implements SyslogServerConfigIF {

    public abstract Class getSyslogServerClass();

    protected String charSet = CHAR_SET_DEFAULT;

    protected long shutdownWait = SyslogConstants.SERVER_SHUTDOWN_WAIT_DEFAULT;

    protected List eventHandlers = new ArrayList();

    protected boolean useStructuredData = USE_STRUCTURED_DATA_DEFAULT;

    protected Object dateTimeFormatter = null;

    protected boolean useDaemonThread = USE_DAEMON_THREAD_DEFAULT;
    protected int threadPriority = THREAD_PRIORITY_DEFAULT;

    public String getCharSet() {
        return this.charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    public long getShutdownWait() {
        return this.shutdownWait;
    }

    public void setShutdownWait(long shutdownWait) {
        this.shutdownWait = shutdownWait;
    }

    public List getEventHandlers() {
        return this.eventHandlers;
    }

    public void addEventHandler(SyslogServerEventHandlerIF eventHandler) {
        this.eventHandlers.add(eventHandler);
    }

    public void insertEventHandler(int pos, SyslogServerEventHandlerIF eventHandler) {
        this.eventHandlers.add(pos, eventHandler);
    }

    public void removeEventHandler(SyslogServerEventHandlerIF eventHandler) {
        this.eventHandlers.remove(eventHandler);
    }

    public void removeAllEventHandlers() {
        this.eventHandlers.clear();
    }

    public boolean isUseStructuredData() {
        return useStructuredData;
    }

    public void setUseStructuredData(boolean useStructuredData) {
        this.useStructuredData = useStructuredData;
    }

    public boolean isUseDaemonThread() {
        return useDaemonThread;
    }

    public Object getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(Object dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public void setUseDaemonThread(boolean useDaemonThread) {
        this.useDaemonThread = useDaemonThread;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }
}
