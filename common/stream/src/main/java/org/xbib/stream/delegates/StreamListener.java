package org.xbib.stream.delegates;

import org.xbib.stream.Stream;

/**
 * A listener of key events in the iteration of a target {@link Stream}.
 */
public interface StreamListener {

    /**
     * Invoked after the first element of the target {@link Stream} has been iterated over.
     */
    void onStart();

    /**
     * Invoked after the last element of the target {@link Stream} has been iterated over.
     */
    void onEnd();

    /**
     * Invoked then stream is closed.
     */
    void onClose();
}
