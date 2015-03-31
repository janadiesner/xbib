package org.snmp4j.smi;

import java.net.InetAddress;

/**
 * The <code>UdpAddress</code> represents UDP/IP transport addresses.
 */
public class UdpAddress extends TransportIpAddress {

    public UdpAddress() {
    }

    public UdpAddress(InetAddress inetAddress, int port) {
        setInetAddress(inetAddress);
        setPort(port);
    }

    public UdpAddress(int port) {
        setPort(port);
    }

    public UdpAddress(String address) {
        if (!parseAddress(address)) {
            throw new IllegalArgumentException(address);
        }
    }

    public static Address parse(String address) {
        UdpAddress a = new UdpAddress();
        if (a.parseAddress(address)) {
            return a;
        }
        return null;
    }

    public boolean equals(Object o) {
        return (o instanceof UdpAddress) && super.equals(o);
    }

}

