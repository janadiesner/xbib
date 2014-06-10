package org.xbib.syslog.server.impl.event.printstream;

import org.xbib.syslog.server.SyslogServerSessionEventHandlerIF;

public class SystemOutSyslogServerEventHandler extends PrintStreamSyslogServerEventHandler {

    public static SyslogServerSessionEventHandlerIF create() {
        return new SystemOutSyslogServerEventHandler();
    }

    public SystemOutSyslogServerEventHandler() {
        super(System.out);
    }
}
