package org.xbib.stream;

/**
 * A callback for a {@link StreamConsumer}.
 *
 * @param <T> the type of stream elements
 */
public interface Callback<T> {

    /**
     * The ongoing iteration.
     */
    static final Iteration iteration = new Iteration();

    /**
     * Implement to consume an element of the stream.
     *
     * @param element the element
     */
    void consume(T element);

}
