package org.snmp4j.security;

import org.snmp4j.asn1.BERSerializable;

/**
 * The <code>SecurityParameters</code> interface represents the security
 * parameters in a SNMPv3 message.
 */
public interface SecurityParameters extends BERSerializable {

    /**
     * Gets the byte position of the first byte (counted from zero) of the
     * security parameters in the whole message.
     *
     * @return the position of the first byte (counted from zero) of the security
     * parameters in the whole SNMP message. -1 is returned, when the position
     * is unknown (not set).
     */
    int getSecurityParametersPosition();

    /**
     * Sets the position of the first byte (counted from zero) of the security
     * parameters in the whole SNMP message.
     *
     * @param pos an integer value >= 0.
     */
    void setSecurityParametersPosition(int pos);

    /**
     * Gets the maximum length of the BER encoded representation of this
     * <code>SecurityParameters</code> instance.
     *
     * @param securityLevel the security level to be used.
     * @return the maximum BER encoded length in bytes.
     */
    int getBERMaxLength(int securityLevel);
}
