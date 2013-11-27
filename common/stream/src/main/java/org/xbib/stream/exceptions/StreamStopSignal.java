package org.xbib.stream.exceptions;

import org.xbib.stream.Callback;
import org.xbib.stream.Stream;

/**
 * Used internally by {@link org.xbib.stream.handlers.FaultHandler}s and {@link Callback}s to require the premature end of an iteration over a
 * {@link Stream}
 */
public class StreamStopSignal extends RuntimeException {

}
