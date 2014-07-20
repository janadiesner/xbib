package org.asynchttpclient;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * A request body which supports random access to its contents.
 */
public interface RandomAccessBody
        extends Body {

    /**
     * Transfers the specified chunk of bytes from this body to the specified channel.
     *
     * @param position The zero-based byte index from which to start the transfer, must not be negative.
     * @param count    The maximum number of bytes to transfer, must not be negative.
     * @param target   The destination channel to transfer the body chunk to, must not be {@code null}.
     * @return The non-negative number of bytes actually transferred.
     * @throws java.io.IOException If the body chunk could not be transferred.
     */
    long transferTo(long position, long count, WritableByteChannel target)
            throws IOException;

}
