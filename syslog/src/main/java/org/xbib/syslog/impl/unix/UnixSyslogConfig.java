package org.xbib.syslog.impl.unix;

import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.AbstractSyslogConfig;

/**
 * UnixSyslogConfig is an extension of AbstractNetSyslogConfig that provides
 * configuration support for Unix-based syslog clients.
 * <p>
 */
public class UnixSyslogConfig extends AbstractSyslogConfig {

    protected String library = SYSLOG_LIBRARY_DEFAULT;
    protected int option;

    public UnixSyslogConfig() {
        // Unix-based syslog does not need localName sent
        this.setSendLocalName(false);
    }

    public Class getSyslogClass() {
        return UnixSyslog.class;
    }

    public String getHost() {
        return null;
    }

    public int getPort() {
        return 0;
    }

    public void setHost(String host) throws SyslogRuntimeException {
        throw new SyslogRuntimeException("Host not appropriate for class \"" + this.getClass().getName() + "\"");
    }

    public void setPort(int port) throws SyslogRuntimeException {
        throw new SyslogRuntimeException("Port not appropriate for class \"" + this.getClass().getName() + "\"");
    }

    public String getLibrary() {
        return this.library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public int getOption() {
        return this.option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public int getMaxQueueSize() {
        throw new SyslogRuntimeException("UnixSyslog protocol does not uses a queueing mechanism");
    }

    public void setMaxQueueSize(int maxQueueSize) {
        throw new SyslogRuntimeException("UnixSyslog protocol does not uses a queueing mechanism");
    }
}
