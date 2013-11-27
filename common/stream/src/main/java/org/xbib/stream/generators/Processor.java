package org.xbib.stream.generators;

import org.xbib.stream.Stream;

/**
 * A {@link org.xbib.stream.generators.Filter} that changes the elements of the input {@link Stream}.
 *
 * @param <E> the type of input elements
 * @author Fabio Simeoni
 */
public abstract class Processor<E> extends Filter<E, E> {

    @Override
    public final E yield(E element) {

        process(element);
        return element;
    }

    ;

    /**
     * Processes an element of a {@link Stream}.
     *
     * @param element the input element
     * @throws org.xbib.stream.exceptions.StreamSkipSignal
     *                          if no element <em>should</em> be yielded from the input element (i.e. the element
     *                          should not contribute to the output stream)
     * @throws RuntimeException ion if no element <em>can</em> be yielded from the input element
     */
    protected abstract void process(E element);
}
