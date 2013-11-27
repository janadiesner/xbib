package org.xbib.stream.test;

import org.xbib.stream.Stream;

/**
 * Generates a {@link Stream} for testing purposes.
 */
public interface StreamProvider {

    /**
     * Generates a {@link Stream}
     *
     * @return the stream.
     */
    Stream<?> get();
}
