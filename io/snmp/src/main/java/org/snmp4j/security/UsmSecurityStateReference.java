package org.snmp4j.security;

/**
 * The <code>UsmSecurityStateReference</code> holds cached security data
 * for the {@link org.snmp4j.security.USM} security model.
 */
public class UsmSecurityStateReference implements SecurityStateReference {

    private byte[] userName;
    private byte[] securityName;
    private byte[] securityEngineID;
    private AuthenticationProtocol authenticationProtocol;
    private PrivacyProtocol privacyProtocol;
    private byte[] authenticationKey;
    private byte[] privacyKey;
    private int securityLevel;

    public UsmSecurityStateReference() {
    }

    public byte[] getUserName() {
        return userName;
    }

    public void setUserName(byte[] userName) {
        this.userName = userName;
    }

    public byte[] getSecurityName() {
        return securityName;
    }

    public void setSecurityName(byte[] securityName) {
        this.securityName = securityName;
    }

    public byte[] getSecurityEngineID() {
        return securityEngineID;
    }

    public void setSecurityEngineID(byte[] securityEngineID) {
        this.securityEngineID = securityEngineID;
    }

    public AuthenticationProtocol getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    public void setAuthenticationProtocol(AuthenticationProtocol authenticationProtocol) {
        this.authenticationProtocol = authenticationProtocol;
    }

    public PrivacyProtocol getPrivacyProtocol() {
        return privacyProtocol;
    }

    public void setPrivacyProtocol(PrivacyProtocol privacyProtocol) {
        this.privacyProtocol = privacyProtocol;
    }

    public byte[] getAuthenticationKey() {
        return authenticationKey;
    }

    public void setAuthenticationKey(byte[] authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public byte[] getPrivacyKey() {
        return privacyKey;
    }

    public void setPrivacyKey(byte[] privacyKey) {
        this.privacyKey = privacyKey;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }
}
