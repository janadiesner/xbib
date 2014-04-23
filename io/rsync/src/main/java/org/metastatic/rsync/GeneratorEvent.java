
package org.metastatic.rsync;

import java.util.EventObject;

/**
 * Generator events are created whenever a checksum pair has been
 * created.
 *
 * @see GeneratorListener
 * @see GeneratorStream
 */
public class GeneratorEvent extends EventObject {

    /**
     * Create a new generator event.
     *
     * @param pair The checksum pair.
     */
    public GeneratorEvent(ChecksumPair pair) {
        super(pair);
    }

    /**
     * Returns the source of this event, already cast to a ChecksumPair.
     *
     * @return The checksum pair.
     */
    public ChecksumPair getChecksumPair() {
        return (ChecksumPair) source;
    }
}
