
package org.metastatic.rsync;

import java.io.IOException;
import java.util.EventListener;

/**
 * A listener for {@link org.metastatic.rsync.RebuilderEvent}s. Rebuilder events contain two values:
 * an offset into the rebuilt file and a block of bytes to be written there.
 *
 */
public interface RebuilderListener extends EventListener {

    /**
     * Upate this listener with an event.
     *
     * @param event The event.
     * @throws java.io.IOException
     */
    void update(RebuilderEvent event) throws ListenerException, IOException;
}
