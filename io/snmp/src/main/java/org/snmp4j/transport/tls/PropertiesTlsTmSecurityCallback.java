package org.snmp4j.transport.tls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.util.SnmpConfigurator;

import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * The <code>PropertiesTlsTmSecurityCallback</code> resolves the
 * <code>tmSecurityName</code> for incoming requests by using the
 * (system) properties
 * <code>org.snmp4j.arg.securityName</code>
 * <code>org.snmp4j.arg.tlsLocalID</code>
 * <code>org.snmp4j.arg.tlsTrustCA</code>
 * <code>org.snmp4j.arg.tlsPeerID</code>
 */
public class PropertiesTlsTmSecurityCallback
        implements TlsTmSecurityCallback<X509Certificate> {

    private static final Logger logger = LogManager.getLogger(PropertiesTlsTmSecurityCallback.class);

    private boolean serverMode;
    private Properties properties;

    public PropertiesTlsTmSecurityCallback(boolean serverMode) {
        this(System.getProperties(), serverMode);
    }

    public PropertiesTlsTmSecurityCallback(Properties properties, boolean serverMode) {
        this.serverMode = serverMode;
        if (properties == null) {
            throw new NullPointerException();
        }
        this.properties = properties;
    }

    @Override
    public OctetString getSecurityName(X509Certificate[] peerCertificateChain) {
        String securityName = properties.getProperty(SnmpConfigurator.P_SECURITY_NAME, null);
        if (securityName != null) {
            return new OctetString(securityName);
        }
        return null;
    }

    @Override
    public boolean isClientCertificateAccepted(X509Certificate peerEndCertificate) {
        String accepted = properties.getProperty(SnmpConfigurator.P_TLS_LOCAL_ID, "");
        if (serverMode) {
            accepted = properties.getProperty(SnmpConfigurator.P_TLS_PEER_ID, "");
        }
        return peerEndCertificate.getSubjectDN().getName().equals(accepted);
    }

    @Override
    public boolean isServerCertificateAccepted(X509Certificate[] peerCertificateChain) {
        String acceptedPeer = properties.getProperty(SnmpConfigurator.P_TLS_PEER_ID, "");
        if (serverMode) {
            acceptedPeer = properties.getProperty(SnmpConfigurator.P_TLS_LOCAL_ID, "");
        }
        String subject = peerCertificateChain[0].getSubjectDN().getName();
        if (subject.equals(acceptedPeer)) {
            return true;
        } else {
            logger.warn("Certificate subject '" + subject +
                    "' does not match accepted peer '" + acceptedPeer + "'");
        }
        String acceptedCA = properties.getProperty(SnmpConfigurator.P_TLS_TRUST_CA, "");
        for (int i = 1; i < peerCertificateChain.length; i++) {
            String ca = peerCertificateChain[i].getIssuerDN().getName();
            if (ca.equals(acceptedCA)) {
                return true;
            } else {
                logger.warn("Certification authority '" + ca +
                        "' does not match accepted CA '" + acceptedCA + "'");
            }
        }
        return false;
    }

    @Override
    public boolean isAcceptedIssuer(X509Certificate issuerCertificate) {
        String acceptedCA = properties.getProperty(SnmpConfigurator.P_TLS_TRUST_CA, "");
        return issuerCertificate.getIssuerDN().getName().equals(acceptedCA);
    }

    @Override
    public String getLocalCertificateAlias(Address targetAddress) {
        return properties.getProperty(SnmpConfigurator.P_TLS_LOCAL_ID);
    }

}
