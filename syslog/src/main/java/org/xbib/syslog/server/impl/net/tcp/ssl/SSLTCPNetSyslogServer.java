package org.xbib.syslog.server.impl.net.tcp.ssl;

import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.server.impl.net.tcp.TCPNetSyslogServer;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;

/**
 * SSLTCPNetSyslogServer provides a simple threaded TCP/IP server implementation
 * which uses SSL/TLS.
 */
public class SSLTCPNetSyslogServer extends TCPNetSyslogServer {

    public void initialize() throws SyslogRuntimeException {
        super.initialize();

        SSLTCPNetSyslogServerConfigIF sslTcpNetSyslogServerConfig = (SSLTCPNetSyslogServerConfigIF) this.tcpNetSyslogServerConfig;

        String keyStore = sslTcpNetSyslogServerConfig.getKeyStore();

        if (keyStore != null && !"".equals(keyStore.trim())) {
            System.setProperty("javax.net.ssl.keyStore", keyStore);
        }

        String keyStorePassword = sslTcpNetSyslogServerConfig.getKeyStorePassword();

        if (keyStorePassword != null && !"".equals(keyStorePassword.trim())) {
            System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        }

        String trustStore = sslTcpNetSyslogServerConfig.getTrustStore();

        if (trustStore != null && !"".equals(trustStore.trim())) {
            System.setProperty("javax.net.ssl.trustStore", trustStore);
        }

        String trustStorePassword = sslTcpNetSyslogServerConfig.getTrustStorePassword();

        if (trustStorePassword != null && !"".equals(trustStorePassword.trim())) {
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        }
    }

    protected ServerSocketFactory getServerSocketFactory() throws IOException {
        ServerSocketFactory serverSocketFactory = SSLServerSocketFactory.getDefault();

        return serverSocketFactory;
    }
}
