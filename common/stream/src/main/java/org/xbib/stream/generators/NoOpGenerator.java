package org.xbib.stream.generators;

/**
 * A pass-through {@link org.xbib.stream.generators.Generator}.
 *
 * @param <E> the type of stream elements
 */
public class NoOpGenerator<E> implements Generator<E, E> {

    @Override
    public E yield(E element) {
        return element;
    }

    ;
}
