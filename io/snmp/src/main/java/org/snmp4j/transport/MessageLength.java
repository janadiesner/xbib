package org.snmp4j.transport;

/**
 * The <code>MessageLength</code> object contains information about the
 * length of a message and the length of its header.
 */
public class MessageLength {

    private int payloadLength;
    private int headerLength;

    /**
     * Constructs a MessageLength object.
     *
     * @param headerLength  the length in bytes of the message header.
     * @param payloadLength the length of the payload.
     */
    public MessageLength(int headerLength, int payloadLength) {
        this.payloadLength = payloadLength;
        this.headerLength = headerLength;
    }

    /**
     * Returns the length of the payload.
     *
     * @return the length in bytes.
     */
    public int getPayloadLength() {
        return payloadLength;
    }

    /**
     * Returns the length of the header.
     *
     * @return the length in bytes.
     */
    public int getHeaderLength() {
        return headerLength;
    }

    /**
     * Returns the total message length (header+payload).
     *
     * @return the sum of {@link #getHeaderLength()} and {@link #getPayloadLength()}.
     */
    public int getMessageLength() {
        return headerLength + payloadLength;
    }

    public String toString() {
        return MessageLength.class.getName() +
                "[headerLength=" + headerLength + ",payloadLength=" + payloadLength + "]";
    }
}
