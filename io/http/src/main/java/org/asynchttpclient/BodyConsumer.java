package org.asynchttpclient;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A simple API to be used with the {@link SimpleAsyncHttpClient} class in order to process response's bytes.
 */
public interface BodyConsumer extends Closeable {

    /**
     * Consume the received bytes.
     *
     * @param byteBuffer a {@link java.nio.ByteBuffer} represntation of the response's chunk.
     * @throws java.io.IOException
     */
    void consume(ByteBuffer byteBuffer) throws IOException;

    /**
     * Invoked when all the response bytes has been processed.
     *
     * @throws java.io.IOException
     */
    void close() throws IOException;

}
