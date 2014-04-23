
package org.metastatic.rsync;

import java.util.EventListener;

/**
 * Standard interface for the hashtable matcher callback, called by
 * {@link org.metastatic.rsync.MatcherStream} when new deltas are ready.
 *
 */
public interface MatcherListener extends EventListener {

    /**
     * Update with a single, new delta.
     *
     * @param event The next delta event.
     */
    void update(MatcherEvent event) throws ListenerException;
}
