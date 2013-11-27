package org.xbib.stream.generators;

import org.xbib.stream.Iteration;
import org.xbib.stream.Stream;
import org.xbib.stream.exceptions.StreamSkipSignal;
import org.xbib.stream.exceptions.StreamStopSignal;

/**
 * Yields elements of a {@link Stream} from elements of another {@link Stream}.
 *
 * @param <E1> the type of elements in the input stream
 * @param <E2> the type of elements in the output stream
 * @see Stream
 */
public interface Generator<E1, E2> {

    /**
     * The ongoing iteration.
     */
    static final Iteration iteration = new Iteration();


    /**
     * Yields an element of a {@link Stream} from an element of another {@link Stream}.
     *
     * @param element the input element
     * @return the output element
     * @throws org.xbib.stream.exceptions.StreamSkipSignal
     *                          if no element <em>should</em> be yielded from the input element (i.e. the element should
     *                          not contribute to the output stream)
     * @throws org.xbib.stream.exceptions.StreamStopSignal
     *                          if no further element should be yielded
     * @throws RuntimeException if no element <em>can</em> be yielded from the input element
     */
    E2 yield(E1 element) throws StreamSkipSignal, StreamStopSignal;
}
