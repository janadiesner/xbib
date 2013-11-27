package org.xbib.stream.generators;

import org.xbib.stream.exceptions.StreamSkipSignal;

/**
 * A partial implementation of {@link org.xbib.stream.generators.Filter} that provides support for skipping elements
 *
 * @param <E1> the type of input elements
 * @param <E2> the type of yielded elements
 */
public abstract class Filter<E1, E2> implements Generator<E1, E2> {

    private final StreamSkipSignal skip = new StreamSkipSignal();

    /**
     * Invoked to skip the current element.
     */
    protected void skip() {
        throw skip;
    }
}
