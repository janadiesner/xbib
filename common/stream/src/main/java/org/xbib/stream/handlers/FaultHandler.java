package org.xbib.stream.handlers;

import org.xbib.stream.Iteration;
import org.xbib.stream.Stream;

/**
 * Handlers of {@link Stream} iteration failures.
 */
public interface FaultHandler {

    /**
     * The ongoing iteration.
     */
    static final Iteration iteration = new Iteration();

    /**
     * Indicates whether iteration should continue or stop the iteration on the occurrence of an iteration failure.
     *
     * @param failure the failure
     * @throws RuntimeException if no element can be yielded from the input element
     */
    void handle(RuntimeException failure);

}
