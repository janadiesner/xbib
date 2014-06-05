package org.xbib.syslog.impl.backlog.printstream;

import org.xbib.syslog.SyslogIF;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.backlog.AbstractSyslogBackLogHandler;

import java.io.PrintStream;

/**
 * PrintStreamSyslogBackLogHandler provides a last-chance mechanism to log messages that fail
 * (for whatever reason) within the rest of Syslog to a PrintStream.
 */
public class PrintStreamSyslogBackLogHandler extends AbstractSyslogBackLogHandler {
    protected PrintStream printStream = null;
    protected boolean appendLinefeed = false;

    public PrintStreamSyslogBackLogHandler(PrintStream printStream) {
        this.printStream = printStream;

        initialize();
    }

    public PrintStreamSyslogBackLogHandler(PrintStream printStream, boolean appendLinefeed) {
        this.printStream = printStream;
        this.appendLinefeed = appendLinefeed;

        initialize();
    }

    public PrintStreamSyslogBackLogHandler(PrintStream printStream, boolean appendLinefeed, boolean appendReason) {
        this.printStream = printStream;
        this.appendLinefeed = appendLinefeed;
        this.appendReason = appendReason;

        initialize();
    }

    public void initialize() throws SyslogRuntimeException {
        if (this.printStream == null) {
            throw new SyslogRuntimeException("PrintStream cannot be null");
        }
    }

    public void down(SyslogIF syslog, String reason) {
        this.printStream.println(syslog.getProtocol() + ": DOWN" + (reason != null && !"".equals(reason.trim()) ? " (" + reason + ")" : ""));
    }

    public void up(SyslogIF syslog) {
        this.printStream.println(syslog.getProtocol() + ": UP");
    }

    public void log(SyslogIF syslog, int level, String message, String reason) {
        String combinedMessage = combine(syslog, level, message, reason);

        if (this.appendLinefeed) {
            this.printStream.println(combinedMessage);

        } else {
            this.printStream.print(combinedMessage);
        }
    }
}
