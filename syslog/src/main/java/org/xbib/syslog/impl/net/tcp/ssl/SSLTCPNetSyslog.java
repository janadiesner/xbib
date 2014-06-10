package org.xbib.syslog.impl.net.tcp.ssl;

import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.net.tcp.TCPNetSyslog;

/**
 * SSLTCPNetSyslog is an extension of AbstractSyslog that provides support for
 * TCP/IP-based (over SSL/TLS) syslog clients.
 */
public class SSLTCPNetSyslog extends TCPNetSyslog {

    public void initialize() throws SyslogRuntimeException {
        super.initialize();

        SSLTCPNetSyslogConfigIF sslTcpNetSyslogConfig = (SSLTCPNetSyslogConfigIF) this.tcpNetSyslogConfig;

        String keyStore = sslTcpNetSyslogConfig.getKeyStore();

        if (keyStore != null && !"".equals(keyStore.trim())) {
            System.setProperty("javax.net.ssl.keyStore", keyStore);
        }

        String keyStorePassword = sslTcpNetSyslogConfig.getKeyStorePassword();

        if (keyStorePassword != null && !"".equals(keyStorePassword.trim())) {
            System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        }

        String trustStore = sslTcpNetSyslogConfig.getTrustStore();

        if (trustStore != null && !"".equals(trustStore.trim())) {
            System.setProperty("javax.net.ssl.trustStore", trustStore);
        }

        String trustStorePassword = sslTcpNetSyslogConfig.getTrustStorePassword();

        if (trustStorePassword != null && !"".equals(trustStorePassword.trim())) {
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        }
    }
}
