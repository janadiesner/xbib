package org.asynchttpclient.consumers;

import org.asynchttpclient.BodyConsumer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A simple {@link java.io.OutputStream} implementation for {@link BodyConsumer}
 */
public class OutputStreamBodyConsumer implements BodyConsumer {

    private final OutputStream outputStream;

    public OutputStreamBodyConsumer(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void consume(ByteBuffer byteBuffer) throws IOException {
        outputStream.write(byteBuffer.array(),
                           byteBuffer.arrayOffset() + byteBuffer.position(),
                           byteBuffer.remaining());
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
