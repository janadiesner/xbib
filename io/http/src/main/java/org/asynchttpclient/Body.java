package org.asynchttpclient;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A request body.
 */
public interface Body extends Closeable {

    /**
     * Gets the length of the body.
     *
     * @return The length of the body in bytes, or negative if unknown.
     */
    long getContentLength();

    /**
     * Reads the next chunk of bytes from the body.
     *
     * @param buffer The buffer to store the chunk in, must not be {@code null}.
     * @return The non-negative number of bytes actually read or {@code -1} if the body has been read completely.
     * @throws java.io.IOException If the chunk could not be read.
     */
    long read(ByteBuffer buffer) throws IOException;

    /**
     * Releases any resources associated with this body.
     *
     * @throws java.io.IOException
     */
    void close() throws IOException;

}
