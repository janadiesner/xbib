package org.snmp4j.security;

import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.OctetString;

import java.io.IOException;

/**
 * {@link org.snmp4j.security.SecurityParameters} implementation for the {@link org.snmp4j.security.TSM}
 * security model.
 */
public class TsmSecurityParameters extends OctetString implements SecurityParameters {

    private int securityParametersPosition;
    private int decodedLength = -1;

    public TsmSecurityParameters() {
        super();
    }

    @Override
    public int getSecurityParametersPosition() {
        return securityParametersPosition;
    }

    @Override
    public void setSecurityParametersPosition(int pos) {
        this.securityParametersPosition = pos;
    }

    @Override
    public int getBERMaxLength(int securityLevel) {
        return getBERLength();
    }

    @Override
    public void decodeBER(BERInputStream inputStream) throws IOException {
        long startPos = inputStream.getPosition();
        super.decodeBER(inputStream);
        decodedLength = (int) (inputStream.getPosition() - startPos);
    }

    /**
     * Gets the position of the {@link org.snmp4j.ScopedPDU}.
     *
     * @return the start position in the {@link org.snmp4j.asn1.BERInputStream}.
     */
    public int getScopedPduPosition() {
        if (decodedLength >= 0) {
            return decodedLength + getSecurityParametersPosition();
        } else {
            return getSecurityParametersPosition() + getBERLength();
        }
    }

}
