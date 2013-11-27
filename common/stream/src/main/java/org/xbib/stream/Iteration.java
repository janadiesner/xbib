package org.xbib.stream;

import org.xbib.stream.exceptions.StreamSkipSignal;
import org.xbib.stream.exceptions.StreamStopSignal;

/**
 * A model of a {@link Stream} iteration, with facilities to control it from within {@link Callback}s and {@link org.xbib.stream.handlers.FaultHandler}s.
 */
public final class Iteration {

    /**
     * Stops the ongoing iteration.
     */
    public void stop() throws StreamStopSignal {
        throw new StreamStopSignal();
    }

    /**
     * Skip this element of the ongoing iteration.
     */
    public void skip() throws StreamSkipSignal {
        throw new StreamSkipSignal();
    }


}
