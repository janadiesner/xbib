package org.xbib.syslog.server.impl.event.printstream;

import org.xbib.syslog.server.SyslogServerEventIF;
import org.xbib.syslog.server.SyslogServerIF;
import org.xbib.syslog.server.SyslogServerSessionEventHandlerIF;
import org.xbib.syslog.util.SyslogUtility;

import java.io.PrintStream;
import java.net.SocketAddress;
import java.util.Date;

/**
 * SystemOutSyslogServerEventHandler provides a simple example implementation
 * of the SyslogServerEventHandlerIF which writes the events to System.out.
 */
public class PrintStreamSyslogServerEventHandler implements SyslogServerSessionEventHandlerIF {

    protected PrintStream stream = null;

    public PrintStreamSyslogServerEventHandler(PrintStream stream) {
        this.stream = stream;
    }

    public void initialize(SyslogServerIF syslogServer) {
    }

    public Object sessionOpened(SyslogServerIF syslogServer, SocketAddress socketAddress) {
        return null;
    }

    public void event(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event) {
        String date = (event.getDate() == null ? new Date() : event.getDate()).toString();
        String facility = SyslogUtility.getFacilityString(event.getFacility());
        String level = SyslogUtility.getLevelString(event.getLevel());

        this.stream.println("{" + facility + "} " + date + " " + level + " " + event.getMessage());
    }

    public void exception(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, Exception exception) {
        //
    }

    public void sessionClosed(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, boolean timeout) {
        //
    }

    public void destroy(SyslogServerIF syslogServer) {
        return;
    }
}
