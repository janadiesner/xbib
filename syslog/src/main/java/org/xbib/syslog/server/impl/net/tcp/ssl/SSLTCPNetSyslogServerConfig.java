package org.xbib.syslog.server.impl.net.tcp.ssl;

import org.xbib.syslog.server.impl.net.tcp.TCPNetSyslogServerConfig;

/**
 * SSLTCPNetSyslogServerConfig provides configuration for SSLTCPNetSyslogServer.
 */
public class SSLTCPNetSyslogServerConfig extends TCPNetSyslogServerConfig implements SSLTCPNetSyslogServerConfigIF {

    protected String keyStore = null;
    protected String keyStorePassword = null;

    protected String trustStore = null;
    protected String trustStorePassword = null;

    public String getKeyStore() {
        return this.keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
        return this.keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStore() {
        return this.trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return this.trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public Class getSyslogServerClass() {
        return SSLTCPNetSyslogServer.class;
    }
}
