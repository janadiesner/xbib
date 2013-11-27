package org.xbib.stream.adapters;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

/**
 * An adaptation strategy for {@link org.xbib.stream.adapters.IteratorStream}.
 */
public class IteratorAdapter<E> implements Closeable {

    private final Iterator<E> iterator;

    /**
     * Creates an instance from a given {@link java.util.Iterator}.
     *
     * @param iterator the iterator
     */
    public IteratorAdapter(Iterator<E> iterator) {
        this.iterator = iterator;
    }

    /**
     * Returns the underlying iterator.
     *
     * @return the iterator
     */
    final Iterator<E> iterator() {
        return iterator;
    }

    /**
     * Returns a locator for the underlying iterator.
     * <p/>
     * By default it returns a locator of the form {@code local:<toString()>}, where {@code <toString()>} is the string
     * obtained by invoking {@link #toString()} on the iterator.
     *
     * @return the locator
     */
    public URI locator() {
        return URI.create("local://" + iterator);
    }

    /**
     * Closes the underlying iterator.
     * <p/>
     * By defaults it has no effect except when the iterator implements the {@link java.io.Closeable} interface. In this
     * case, it simply delegates to the iterator.
     */
    @Override
    public void close() throws IOException {
        if (iterator instanceof Closeable) {
            ((Closeable) iterator).close();
        }
    }

    ;


}
