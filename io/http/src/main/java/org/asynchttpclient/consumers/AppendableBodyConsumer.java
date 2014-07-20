package org.asynchttpclient.consumers;

import org.asynchttpclient.BodyConsumer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An {@link Appendable} customer for {@link java.nio.ByteBuffer}
 */
public class AppendableBodyConsumer implements BodyConsumer {

    private final Appendable appendable;
    private final String encoding;

    public AppendableBodyConsumer(Appendable appendable, String encoding) {
        this.appendable = appendable;
        this.encoding = encoding;
    }

    public AppendableBodyConsumer(Appendable appendable) {
        this.appendable = appendable;
        this.encoding = "UTF-8";
    }

    @Override
    public void consume(ByteBuffer byteBuffer) throws IOException {
        appendable.append(new String(byteBuffer.array(),
                                     byteBuffer.arrayOffset() + byteBuffer.position(),
                                     byteBuffer.remaining(),
                                     encoding));
    }

    @Override
    public void close() throws IOException {
        if (appendable instanceof Closeable) {
            Closeable.class.cast(appendable).close();
        }
    }
}
