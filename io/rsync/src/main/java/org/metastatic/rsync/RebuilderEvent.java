
package org.metastatic.rsync;

import java.util.EventObject;

/**
 * a rebuilder event. Rebuilder events are emitted by a {@link
 * RebuilderStream} each time a new {@link org.metastatic.rsync.Delta} is applied. The stream
 * will send this event to each of its {@link RebuilderListener}s.
 *
 * @see RebuilderStream
 * @see RebuilderListener
 */
public class RebuilderEvent extends EventObject {

    /**
     * The destination offset.
     */
    protected transient long offset;

    /**
     * Create a new rebuilder event.
     *
     * @param data   The source of this event, the data block.
     * @param offset The destination offset.
     */
    public RebuilderEvent(byte[] data, long offset) {
        this(data, 0, data.length, offset);
    }

    public RebuilderEvent(byte[] data, int off, int len, long offset) {
        super(new byte[len]);
        System.arraycopy(data, off, source, 0, len);
        this.offset = offset;
    }

    /**
     * Get the data. This method is equivalent to {@link
     * java.util.EventObject#getSource()} but the source is already cast
     * for convenience.
     *
     * @return The data array.
     */
    public byte[] getData() {
        return (byte[]) source;
    }

    /**
     * Get the offset at which the data should be written.
     *
     * @return The offset.
     */
    public long getOffset() {
        return offset;
    }
}
