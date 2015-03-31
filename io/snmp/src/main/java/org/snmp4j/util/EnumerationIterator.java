package org.snmp4j.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * The <code>EnumerationIterator</code> provides an iterator from an
 * {@link java.util.Enumeration}.
 */
public class EnumerationIterator<E> implements Iterator<E> {

    private Enumeration<E> e;

    public EnumerationIterator(Enumeration<E> e) {
        this.e = e;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements.
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        return e.hasMoreElements();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     */
    public E next() {
        return e.nextElement();
    }

    /**
     * This method is not supported for enumerations.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
