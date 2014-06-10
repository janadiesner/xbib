package org.xbib.syslog.impl.net.tcp.ssl;

import org.xbib.syslog.impl.net.tcp.TCPNetSyslogWriter;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 * SSLTCPNetSyslogWriter is an implementation of Runnable that supports sending
 * TCP/IP-based (over SSL/TLS) messages within a separate Thread.
 * <p>
 * <p>When used in "threaded" mode (see TCPNetSyslogConfig for the option),
 * a queuing mechanism is used (via LinkedList).</p>
 */
public class SSLTCPNetSyslogWriter extends TCPNetSyslogWriter {

    protected SocketFactory obtainSocketFactory() {
        return SSLSocketFactory.getDefault();
    }
}
