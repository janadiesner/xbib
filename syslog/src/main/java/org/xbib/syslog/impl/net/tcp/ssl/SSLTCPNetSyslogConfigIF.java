package org.xbib.syslog.impl.net.tcp.ssl;

import org.xbib.syslog.impl.net.tcp.TCPNetSyslogConfigIF;

/**
 * SSLTCPNetSyslogConfigIF is a configuration interface supporting TCP/IP-based
 * (over SSL/TLS) Syslog implementations.
 */
public interface SSLTCPNetSyslogConfigIF extends TCPNetSyslogConfigIF {
    public String getKeyStore();

    public void setKeyStore(String keyStore);

    public String getKeyStorePassword();

    public void setKeyStorePassword(String keyStorePassword);

    public String getTrustStore();

    public void setTrustStore(String trustStore);

    public String getTrustStorePassword();

    public void setTrustStorePassword(String trustStorePassword);
}
