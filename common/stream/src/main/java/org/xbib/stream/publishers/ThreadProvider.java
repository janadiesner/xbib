package org.xbib.stream.publishers;

import org.xbib.stream.Stream;

/**
 * Provides {@link Thread}s for the asynchronous publicaton of {@link Stream}.
 */
public interface ThreadProvider {

    /**
     * Provides a new {@link Thread} in which to execute the publication task.
     *
     * @param task the task
     * @return the {@link Thread}
     */
    Thread newThread(Runnable task);
}
