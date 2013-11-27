package org.xbib.stream.exceptions;

import org.xbib.stream.Stream;
import org.xbib.stream.generators.Generator;

/**
 * Used in {@link Generator}s or {@link org.xbib.stream.handlers.FaultHandler}s  to signals that the current element of a {@link Stream} should be skipped.
 */
public class StreamSkipSignal extends StreamException {

    /**
     * Creates an instance.
     */
    public StreamSkipSignal() {
        super();
    }


}
