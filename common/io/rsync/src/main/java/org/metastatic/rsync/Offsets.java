
package org.metastatic.rsync;

/**
 * This class represents an update to a file or array of bytes wherein
 * the bytes themselves have not changed, but have moved to another
 * location. This is represented by three fields: the offset in the
 * original data, the offset in the new data, and the length, in bytes,
 * of this block.
 *
 */
public class Offsets implements Delta {

    /**
     * The original offset.
     *
     */
    protected long oldOffset;

    /**
     * The new offset.
     *
     */
    protected long newOffset;

    /**
     * The size of the moved block, in bytes.
     *
     */
    protected int blockLength;

    /**
     * Create a new pair of offsets. The idea behind this object is
     * that this sort of {@link org.metastatic.rsync.Delta} represents original data
     * that has simply moved in the new data.
     *
     * @param oldOffset   The offset in the original data.
     * @param newOffset   The offset in the new data.
     * @param blockLength The size, in bytes, of the block that has moved.
     */
    public Offsets(long oldOffset, long newOffset, int blockLength) {
        this.oldOffset = oldOffset;
        this.newOffset = newOffset;
        this.blockLength = blockLength;
    }

    // Delta interface implementation.

    public long getWriteOffset() {
        return newOffset;
    }

    public int getBlockLength() {
        return blockLength;
    }

    // Property accessor methods

    /**
     * Get the original offset.
     *
     * @return The original offset.
     */
    public long getOldOffset() {
        return oldOffset;
    }

    /**
     * Get the updated offset.
     *
     * @return The updated offset.
     */
    public long getNewOffset() {
        return newOffset;
    }

    /**
     * Set the block size.
     *
     * @param len The new value for the block size.
     */
    public void setBlockLength(int len) {
        blockLength = len;
    }

    // Public instance methods overriding java.lang.Object -------------

    /**
     * Return a {@link String} representation of this object.
     *
     * @return A string representing this object.
     */
    public String toString() {
        return "[ old=" + oldOffset + " new=" + newOffset
                + " len=" + blockLength + " ]";
    }

    /**
     * Test if one object is equal to this one.
     *
     * @return <tt>true</tt> If <tt>o</tt> is an Offsets instance and the
     *         {@link #oldOffset}, {@link #newOffset}, and {@link
     *         #blockLength} fields are all equal.
     * @throws ClassCastException   If <tt>o</tt> is not an
     *                              instance of this class.
     * @throws NullPointerException If <tt>o</tt> is null.
     */
    public boolean equals(Object o) {
        return oldOffset == ((Offsets) o).oldOffset
                && newOffset == ((Offsets) o).newOffset
                && blockLength == ((Offsets) o).blockLength;
    }

    /**
     * Returns the hash code of this object, defined as:
     * <blockquote>
     * <tt>{@link #oldOffset} + {@link #newOffset} + {@link
     * #blockLength}
     * % 2^32</tt>
     * </blockquote>
     *
     * @return The hash code of this object.
     */
    public int hashCode() {
        return (int) (oldOffset + newOffset + blockLength);
    }
}
