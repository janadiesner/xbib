package org.snmp4j.smi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;

/**
 * The <code>SshAddress</code> represents a SSH transport addresses as defined
 * by RFC 5953 SnmpTSLAddress textual convention.
 */
public class TlsAddress extends TcpAddress {

    private static final Logger logger = LogManager.getLogger(SshAddress.class);

    public TlsAddress() {
        super();
    }

    public TlsAddress(InetAddress inetAddress, int port) {
        super(inetAddress, port);
    }

    public TlsAddress(String address) {
        if (!parseAddress(address)) {
            throw new IllegalArgumentException(address);
        }
    }

    public static Address parse(String address) {
        try {
            TlsAddress a = new TlsAddress();
            if (a.parseAddress(address)) {
                return a;
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return null;
    }

    public boolean equals(Object o) {
        return (o instanceof TlsAddress) && super.equals(o);
    }

}

