package org.xbib.syslog.impl.backlog.printstream;

import org.xbib.syslog.SyslogBackLogHandlerIF;

/**
 * SystemOutSyslogBackLogHandler provides a last-chance mechanism to log messages that fail
 * (for whatever reason) within the rest of Syslog to System.out.
 */
public class SystemOutSyslogBackLogHandler extends PrintStreamSyslogBackLogHandler {
    public static final SyslogBackLogHandlerIF create() {
        return new SystemOutSyslogBackLogHandler();
    }

    public SystemOutSyslogBackLogHandler() {
        super(System.out, true);
    }

    public SystemOutSyslogBackLogHandler(boolean appendReason) {
        super(System.out, true, appendReason);
    }
}
