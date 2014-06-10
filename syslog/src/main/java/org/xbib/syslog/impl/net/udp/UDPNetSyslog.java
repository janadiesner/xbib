package org.xbib.syslog.impl.net.udp;

import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.AbstractSyslogWriter;
import org.xbib.syslog.impl.net.AbstractNetSyslog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * UDPNetSyslog is an extension of AbstractSyslog that provides support for
 * UDP/IP-based syslog clients.
 */
public class UDPNetSyslog extends AbstractNetSyslog {

    protected DatagramSocket socket = null;

    public void initialize() throws SyslogRuntimeException {
        super.initialize();

        createDatagramSocket(true);
    }

    protected synchronized void createDatagramSocket(boolean initialize) {
        try {
            this.socket = new DatagramSocket();

        } catch (SocketException se) {
            if (initialize) {
                if (this.syslogConfig.isThrowExceptionOnInitialize()) {
                    throw new SyslogRuntimeException(se);
                }

            } else {
                throw new SyslogRuntimeException(se);
            }
        }

        if (this.socket == null) {
            throw new SyslogRuntimeException("Cannot seem to get a Datagram socket");
        }
    }

    protected void write(int level, byte[] message) throws SyslogRuntimeException {
        if (this.socket == null) {
            createDatagramSocket(false);
        }

        InetAddress hostAddress = getHostAddress();

        DatagramPacket packet = new DatagramPacket(
                message,
                message.length,
                hostAddress,
                this.syslogConfig.getPort()
        );

        int attempts = 0;

        while (attempts != -1 && attempts < (this.netSyslogConfig.getWriteRetries() + 1)) {
            try {
                this.socket.send(packet);
                attempts = -1;

            } catch (IOException ioe) {
                if (attempts == (this.netSyslogConfig.getWriteRetries() + 1)) {
                    throw new SyslogRuntimeException(ioe);
                }
            }
        }
    }

    public void flush() throws SyslogRuntimeException {
        shutdown();

        createDatagramSocket(true);
    }

    public void shutdown() throws SyslogRuntimeException {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
    }

    public AbstractSyslogWriter getWriter() {
        return null;
    }

    public void returnWriter(AbstractSyslogWriter syslogWriter) {
        //
    }
}
