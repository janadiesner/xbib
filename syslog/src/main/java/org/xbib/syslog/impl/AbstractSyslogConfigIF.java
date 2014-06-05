package org.xbib.syslog.impl;

import org.xbib.syslog.SyslogConfigIF;

import java.util.List;

/**
 * AbstractSyslogConfigIF provides an interface for all Abstract Syslog
 * configuration implementations.
 */
public interface AbstractSyslogConfigIF extends SyslogConfigIF {
    public Class getSyslogWriterClass();

    public List getBackLogHandlers();

    public List getMessageModifiers();

    public byte[] getSplitMessageBeginText();

    public void setSplitMessageBeginText(byte[] beginText);

    public byte[] getSplitMessageEndText();

    public void setSplitMessageEndText(byte[] endText);

    public boolean isThreaded();

    public void setThreaded(boolean threaded);

    public boolean isUseDaemonThread();

    public void setUseDaemonThread(boolean useDaemonThread);

    public int getThreadPriority();

    public void setThreadPriority(int threadPriority);

    public long getThreadLoopInterval();

    public void setThreadLoopInterval(long threadLoopInterval);

    public long getMaxShutdownWait();

    public void setMaxShutdownWait(long maxShutdownWait);

    public int getWriteRetries();

    public void setWriteRetries(int writeRetries);

    public int getMaxQueueSize();

    public void setMaxQueueSize(int maxQueueSize);
}
