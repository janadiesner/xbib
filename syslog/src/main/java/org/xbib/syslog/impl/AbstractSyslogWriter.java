package org.xbib.syslog.impl;

import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.util.SyslogUtility;

import java.util.LinkedList;
import java.util.List;

/**
 * AbstractSyslogWriter is an implementation of Runnable that supports sending
 * syslog messages within a separate Thread or an object pool.
 * <p>
 * <p>When used in "threaded" mode (see TCPNetSyslogConfig for the option),
 * a queuing mechanism is used (via LinkedList).</p>
 */
public abstract class AbstractSyslogWriter implements Runnable {

    protected AbstractSyslog syslog = null;

    protected List queuedMessages = null;

    protected Thread thread = null;

    protected AbstractSyslogConfigIF syslogConfig = null;

    protected boolean shutdown = false;

    public void initialize(AbstractSyslog abstractSyslog) {
        this.syslog = abstractSyslog;
        try {
            this.syslogConfig = (AbstractSyslogConfigIF) this.syslog.getConfig();
        } catch (ClassCastException cce) {
            throw new SyslogRuntimeException("config must implement interface AbstractSyslogConfigIF");
        }
        if (this.syslogConfig.isThreaded()) {
            this.queuedMessages = new LinkedList();
        }
    }

    public void queue(int level, byte[] message) {
        synchronized (this.queuedMessages) {
            if (this.syslogConfig.getMaxQueueSize() == -1 || this.queuedMessages.size() < this.syslogConfig.getMaxQueueSize()) {
                this.queuedMessages.add(message);
            } else {
                this.syslog.backLog(level, SyslogUtility.newString(syslogConfig, message), "MaxQueueSize (" + this.syslogConfig.getMaxQueueSize() + ") reached");
            }
        }
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public boolean hasThread() {
        return this.thread != null && this.thread.isAlive();
    }

    public abstract void write(byte[] message);

    public abstract void flush();

    public abstract void shutdown();

    protected abstract void runCompleted();

    public void run() {
        while (!this.shutdown || !this.queuedMessages.isEmpty()) {
            List queuedMessagesCopy;

            synchronized (this.queuedMessages) {
                queuedMessagesCopy = new LinkedList(this.queuedMessages);
                this.queuedMessages.clear();
            }
            while (!queuedMessagesCopy.isEmpty()) {
                byte[] message = (byte[]) queuedMessagesCopy.remove(0);

                try {
                    write(message);

                    this.syslog.setBackLogStatus(false);

                } catch (SyslogRuntimeException sre) {
                    this.syslog.backLog(SyslogConstants.LEVEL_INFO, SyslogUtility.newString(this.syslog.getConfig(), message), sre);
                }
            }
            try {
                Thread.sleep(this.syslogConfig.getThreadLoopInterval());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        runCompleted();
    }
}
