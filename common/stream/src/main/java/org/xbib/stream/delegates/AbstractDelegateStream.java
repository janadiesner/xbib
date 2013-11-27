package org.xbib.stream.delegates;

import org.xbib.stream.LookAheadStream;
import org.xbib.stream.Stream;

/**
 * Partial implementation for {@link Stream}s that delegate to an underlying streams.
 *
 * @param <E1> the type of elements of the underlying stream
 * @param <E2> the type of elements of the stream delegate
 */
abstract class AbstractDelegateStream<E1, E2> extends LookAheadStream<E2> {

    private final Stream<E1> stream;

    /**
     * Creates an instance that delegates to a given {@link Stream}.
     *
     * @param stream the stream
     */
    AbstractDelegateStream(Stream<E1> stream) {

        if (stream == null) {
            throw new IllegalArgumentException("invalid null stream");
        }

        this.stream = stream;
    }

    /**
     * Returns the underlying {@link Stream}.
     *
     * @return the stream
     */
    protected Stream<E1> stream() {
        return stream;
    }


    @Override
    public void close() {
        stream.close();
    }

    @Override
    public java.net.URI locator() throws IllegalStateException {
        return stream.locator();
    }

    ;

    @Override
    public void remove() {
        stream.remove();
    }

    @Override
    public boolean isClosed() {
        return stream.isClosed();
    }
}
