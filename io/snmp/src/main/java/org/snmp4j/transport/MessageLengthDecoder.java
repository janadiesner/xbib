package org.snmp4j.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The <code>MessageLengthDecoder</code> needs to be implemented for connection
 * oriented transport mappings, because those transport mappings have no message
 * boundaries. To determine the message length, the message header is decoded
 * in a protocol specific way.
 */
public interface MessageLengthDecoder {

    /**
     * Returns the minimum length of the header to be decoded. Typically this
     * is a constant value.
     *
     * @return the minimum length in bytes.
     */
    int getMinHeaderLength();

    /**
     * Returns the total message length to read (including header) and
     * the actual header length.
     *
     * @param buf a ByteBuffer with a minimum of {@link #getMinHeaderLength()}.
     * @return the total message length in bytes and the actual header length in bytes.
     * @throws java.io.IOException if the header cannot be decoded.
     */
    MessageLength getMessageLength(ByteBuffer buf) throws IOException;

}
