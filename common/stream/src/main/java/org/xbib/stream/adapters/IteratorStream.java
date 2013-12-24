package org.xbib.stream.adapters;

import org.xbib.stream.Stream;

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link Stream} adapter for {@link java.util.Iterator}s.
 */
public class IteratorStream<E> implements Stream<E> {

    private final Iterator<E> iterator;

    private final IteratorAdapter<E> adapter;

    private boolean closed;

    /**
     * Creates an instance that adapts a given {@link IteratorAdapter}.
     *
     * @param adapter the adapter
     */
    public IteratorStream(IteratorAdapter<E> adapter) {
        this.iterator = adapter.iterator();
        this.adapter = adapter;
    }


    /**
     * Creates an instance that adapts a given {@link java.util.Iterator} with a default {@link IteratorAdapter}.
     *
     * @param iterator the iterator
     */
    public IteratorStream(final Iterator<E> iterator) {
        this(new IteratorAdapter<E>(iterator)); //use default adapter
    }

    @Override
    public boolean hasNext() {

        if (closed) {
            return false; //respect close semantics
        } else {

            boolean hasNext = iterator.hasNext();

            if (!hasNext) {
                close();
            }

            return hasNext;
        }

    }

    @Override
    public E next() {

        //respect close semantics
        if (closed) {
            throw new NoSuchElementException();
        }

        return iterator.next();
    }


    @Override
    public void close() {

        try {
            adapter.close();
        } catch (Exception e) {
            // log.error("could not close iterator "+locator(),e);
        } finally {
            closed = true;
        }
    }

    @Override
    public URI locator() {
        return adapter.locator();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
}
