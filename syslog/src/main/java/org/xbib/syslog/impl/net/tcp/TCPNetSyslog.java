package org.xbib.syslog.impl.net.tcp;

import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.AbstractSyslogWriter;
import org.xbib.syslog.impl.net.AbstractNetSyslog;

/**
 * TCPNetSyslog is an extension of AbstractSyslog that provides support for
 * TCP/IP-based syslog clients.
 */
public class TCPNetSyslog extends AbstractNetSyslog {

    protected TCPNetSyslogWriter writer = null;

    protected TCPNetSyslogConfigIF tcpNetSyslogConfig = null;

    public void initialize() throws SyslogRuntimeException {
        super.initialize();

        try {
            this.tcpNetSyslogConfig = (TCPNetSyslogConfigIF) this.syslogConfig;

        } catch (ClassCastException cce) {
            throw new SyslogRuntimeException("config must implement interface TCPNetSyslogConfigIF");
        }
    }

    public AbstractSyslogWriter getWriter() {
        return getWriter(true);
    }

    public synchronized AbstractSyslogWriter getWriter(boolean create) {
        if (this.writer != null || !create) {
            return this.writer;
        }

        this.writer = (TCPNetSyslogWriter) createWriter();

        if (this.tcpNetSyslogConfig.isThreaded()) {
            createWriterThread(this.writer);
        }

        return this.writer;
    }

    protected void write(int level, byte[] message) throws SyslogRuntimeException {
        AbstractSyslogWriter syslogWriter = getWriter();

        try {
            if (syslogWriter.hasThread()) {
                syslogWriter.queue(level, message);

            } else {
                syslogWriter.write(message);
            }

        } finally {
            returnWriter(syslogWriter);
        }
    }

    public void flush() throws SyslogRuntimeException {
        AbstractSyslogWriter syslogWriter = getWriter(false);

        if (syslogWriter != null) {
            syslogWriter.flush();
        }
    }

    public void shutdown() throws SyslogRuntimeException {
        AbstractSyslogWriter syslogWriter = getWriter(false);

        if (syslogWriter != null) {
            syslogWriter.shutdown();
        }
    }

    public void returnWriter(AbstractSyslogWriter syslogWriter) {
        //
    }
}
