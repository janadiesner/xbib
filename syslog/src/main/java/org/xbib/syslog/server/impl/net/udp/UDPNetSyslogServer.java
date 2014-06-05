package org.xbib.syslog.server.impl.net.udp;

import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.server.SyslogServerEventIF;
import org.xbib.syslog.server.impl.AbstractSyslogServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * UDPNetSyslogServer provides a simple non-threaded UDP/IP server implementation.
 */
public class UDPNetSyslogServer extends AbstractSyslogServer {

    protected DatagramSocket ds = null;

    public void initialize() throws SyslogRuntimeException {
        //
    }

    public void shutdown() {
        super.shutdown();

        if (this.syslogServerConfig.getShutdownWait() > 0) {
            try {
                Thread.sleep(this.syslogServerConfig.getShutdownWait());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (this.ds != null && !this.ds.isClosed()) {
            this.ds.close();
        }
    }

    protected DatagramSocket createDatagramSocket() throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = null;

        if (this.syslogServerConfig.getHost() != null) {
            InetAddress inetAddress = InetAddress.getByName(this.syslogServerConfig.getHost());

            datagramSocket = new DatagramSocket(this.syslogServerConfig.getPort(), inetAddress);

        } else {
            datagramSocket = new DatagramSocket(this.syslogServerConfig.getPort());
        }

        return datagramSocket;
    }

    public void run() {
        try {
            this.ds = createDatagramSocket();
            this.shutdown = false;

        } catch (SocketException se) {
            return;

        } catch (UnknownHostException uhe) {
            return;
        }

        byte[] receiveData = new byte[SyslogConstants.SYSLOG_BUFFER_SIZE];

        handleInitialize(this);

        while (!this.shutdown) {
            DatagramPacket dp = null;

            try {
                dp = new DatagramPacket(receiveData, receiveData.length);

                this.ds.receive(dp);

                SyslogServerEventIF event = createEvent(this.getConfig(), receiveData, dp.getLength(), dp.getAddress());

                handleEvent(null, this, dp, event);

            } catch (SocketException se) {
                int i = se.getMessage() == null ? -1 : se.getMessage().toLowerCase().indexOf("socket closed");

                if (i == -1) {
                    handleException(null, this, dp.getSocketAddress(), se);
                }

            } catch (IOException ioe) {
                handleException(null, this, dp.getSocketAddress(), ioe);
            }
        }

        handleDestroy(this);
    }
}
