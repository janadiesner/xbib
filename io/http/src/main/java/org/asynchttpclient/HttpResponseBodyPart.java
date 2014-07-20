package org.asynchttpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A callback class used when an HTTP response body is received.
 */
public abstract class HttpResponseBodyPart {

    /**
     * Return length of this part in bytes.
     * 
     */
    public abstract int length();
    
    /**
     * Return the response body's part bytes received.
     *
     * @return the response body's part bytes received.
     */
    public abstract byte[] getBodyPartBytes();

    /**
     * Method for accessing contents of this part via stream.
     */
    public abstract InputStream readBodyPartBytes();
    
    /**
     * Write the available bytes to the {@link java.io.OutputStream}
     *
     * @param outputStream
     * @return The number of bytes written
     * @throws java.io.IOException
     */
    public abstract int writeTo(OutputStream outputStream) throws IOException;

    /**
     * Return a {@link java.nio.ByteBuffer} that wraps the actual bytes read from the response's chunk. The {@link java.nio.ByteBuffer}
     * capacity is equal to the number of bytes available.
     *
     * @return {@link java.nio.ByteBuffer}
     */
    public abstract ByteBuffer getBodyByteBuffer();

    /**
     * Return true if this is the last part.
     *
     * @return true if this is the last part.
     */
    public abstract boolean isLast();

    /**
     * Close the underlying connection once the processing has completed. Invoking that method means the
     * underlying TCP connection will be closed as soon as the processing of the response is completed. That
     * means the underlying connection will never get pooled.
     */
    public abstract void markUnderlyingConnectionAsToBeClosed();

    /**
     * Return true of the underlying connection will be closed once the response has been fully processed.
     *
     * @return true of the underlying connection will be closed once the response has been fully processed.
     */
    public abstract boolean isUnderlyingConnectionToBeClosed();

}
