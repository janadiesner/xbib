package org.snmp4j.smi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;

/**
 * The <code>TcpAddress</code> represents TCP/IP transport addresses.
 */

public class TcpAddress extends TransportIpAddress {

    private static final Logger logger = LogManager.getLogger(TcpAddress.class);

    public TcpAddress() {
        super();
    }

    public TcpAddress(InetAddress inetAddress, int port) {
        setInetAddress(inetAddress);
        setPort(port);
    }

    public TcpAddress(int port) {
        super();
        setPort(port);
    }

    public TcpAddress(String address) {
        if (!parseAddress(address)) {
            throw new IllegalArgumentException(address);
        }
    }

    public static Address parse(String address) {
        try {
            TcpAddress a = new TcpAddress();
            if (a.parseAddress(address)) {
                return a;
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return null;
    }

    public boolean equals(Object o) {
        return (o instanceof TcpAddress) && super.equals(o);
    }

}
