package org.xbib.syslog.impl.backlog;

import org.xbib.syslog.SyslogBackLogHandlerIF;
import org.xbib.syslog.Syslogger;

/**
 * NullSyslogBackLogHandler can be used if there's no need for a last-chance
 * logging mechanism whenever the Syslog protocol fails.
 */
public class NullSyslogBackLogHandler implements SyslogBackLogHandlerIF {

    public void initialize() {
    }

    public void down(Syslogger syslog, String reason) {
    }

    public void up(Syslogger syslog) {
    }

    public void log(Syslogger syslog, int level, String message, String reason) {
    }
}
