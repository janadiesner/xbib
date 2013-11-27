package org.xbib.stream.publishers;

import org.xbib.stream.Stream;

import java.net.URI;

/**
 * Publishes a {@link Stream} at a given address.
 */
public interface StreamPublisher {

    /**
     * Publishes the stream and returns its address.
     *
     * @return the address
     */
    URI publish();
}
