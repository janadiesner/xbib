package org.snmp4j.asn1;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The interface <code>BERSerializable</code> has to be implemented by
 * any data type class that needs to be serialized using the Basic Encoding
 * Rules (BER) that provides enconding rules for ASN.1 data types.
 */
public interface BERSerializable {

    /**
     * Returns the length of this <code>BERSerializable</code> object
     * in bytes when encoded according to the Basic Encoding Rules (BER).
     *
     * @return the BER encoded length of this variable.
     */
    int getBERLength();

    /**
     * Returns the length of the payload of this <code>BERSerializable</code> object
     * in bytes when encoded according to the Basic Encoding Rules (BER).
     *
     * @return the BER encoded length of this variable.
     */
    int getBERPayloadLength();

    /**
     * Decodes a <code>Variable</code> from an <code>InputStream</code>.
     *
     * @param inputStream an <code>InputStream</code> containing a BER encoded byte stream.
     * @throws java.io.IOException if the stream could not be decoded by using BER rules.
     */
    void decodeBER(BERInputStream inputStream) throws IOException;

    /**
     * Encodes a <code>Variable</code> to an <code>OutputStream</code>.
     *
     * @param outputStream an <code>OutputStream</code>.
     * @throws java.io.IOException if an error occurs while writing to the stream.
     */
    void encodeBER(OutputStream outputStream) throws IOException;

}
