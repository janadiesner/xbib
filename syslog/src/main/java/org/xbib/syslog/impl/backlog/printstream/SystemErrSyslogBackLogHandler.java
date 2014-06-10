package org.xbib.syslog.impl.backlog.printstream;

import org.xbib.syslog.SyslogBackLogHandlerIF;

/**
 * SystemErrSyslogBackLogHandler provides a last-chance mechanism to log messages that fail
 * (for whatever reason) within the rest of Syslog to System.err.
 */
public class SystemErrSyslogBackLogHandler extends PrintStreamSyslogBackLogHandler {
    public static SyslogBackLogHandlerIF create() {
        return new SystemErrSyslogBackLogHandler();
    }

    public SystemErrSyslogBackLogHandler() {
        super(System.err, true);
    }

    public SystemErrSyslogBackLogHandler(boolean appendReason) {
        super(System.err, true, appendReason);
    }
}
