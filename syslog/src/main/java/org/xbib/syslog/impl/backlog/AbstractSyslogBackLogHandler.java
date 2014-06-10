package org.xbib.syslog.impl.backlog;

import org.xbib.syslog.SyslogBackLogHandlerIF;
import org.xbib.syslog.SyslogIF;
import org.xbib.syslog.util.SyslogUtility;

/**
 * AbstractSyslogBackLogHandler is an implementation of SyslogBackLogHandlerIF
 * that mainly provides the helpful "combine" method for handling the "reason"
 * why a BackLog has occurred.
 */
public abstract class AbstractSyslogBackLogHandler implements SyslogBackLogHandlerIF {
    protected boolean appendReason = true;

    protected String combine(SyslogIF syslog, int level, String message, String reason) {
        // Note: syslog is explicitly ignored by default

        String _message = message != null ? message : "UNKNOWN";
        String _reason = reason != null ? reason : "UNKNOWN";

        String combinedMessage = SyslogUtility.getLevelString(level) + " " + _message;

        if (this.appendReason) {
            combinedMessage += " [" + _reason + "]";
        }

        return combinedMessage;
    }
}
