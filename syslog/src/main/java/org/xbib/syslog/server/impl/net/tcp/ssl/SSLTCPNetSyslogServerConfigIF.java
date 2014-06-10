package org.xbib.syslog.server.impl.net.tcp.ssl;

import org.xbib.syslog.server.impl.net.tcp.TCPNetSyslogServerConfigIF;

/**
 * SSLTCPNetSyslogServerConfigIF provides configuration for SSLTCPNetSyslogServer.
 */
public interface SSLTCPNetSyslogServerConfigIF extends TCPNetSyslogServerConfigIF {
    public String getKeyStore();

    public void setKeyStore(String keyStore);

    public String getKeyStorePassword();

    public void setKeyStorePassword(String keyStorePassword);

    public String getTrustStore();

    public void setTrustStore(String trustStore);

    public String getTrustStorePassword();

    public void setTrustStorePassword(String trustStorePassword);
}
