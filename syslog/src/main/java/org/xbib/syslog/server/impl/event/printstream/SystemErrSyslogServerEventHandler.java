package org.xbib.syslog.server.impl.event.printstream;

import org.xbib.syslog.server.SyslogServerSessionEventHandlerIF;

public class SystemErrSyslogServerEventHandler extends PrintStreamSyslogServerEventHandler {

    public static SyslogServerSessionEventHandlerIF create() {
        return new SystemErrSyslogServerEventHandler();
    }

    public SystemErrSyslogServerEventHandler() {
        super(System.err);
    }
}
