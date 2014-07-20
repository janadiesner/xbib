package org.metastatic.rsync;

/**
 * This is the {@link org.metastatic.rsync.Delta} in the rsync algorithm that introduces new
 * data. It is an array of bytes and an offset, such that the updated
 * file should contain this block at the given offset.
 *
 */
public class DataBlock implements Delta {

    /**
     * The block of data to insert.
     */
    protected final byte[] data;

    /**
     * The offset in the file to start this block.
     */
    protected final long offset;

    /**
     * Create a new instance of a DataBlock with a given offset and
     * block of bytes.
     *
     * @param offset The offset where this data should go.
     * @param data   The data itself.
     */
    public DataBlock(long offset, byte[] data) {
        this.offset = offset;
        this.data = data.clone();
    }

    /**
     * Create a new instance of a DataBlock with a given offset and a
     * portion of a byte array.
     *
     * @param offset The write offset of this data block.
     * @param data   The data itself.
     * @param off    The offset in the array to begin copying.
     * @param len    The number of bytes to copy.
     */
    public DataBlock(long offset, byte[] data, int off, int len) {
        this.offset = offset;
        if (data.length == len && off == 0) {
            this.data = data.clone();
        } else {
            this.data = new byte[len];
            System.arraycopy(data, 0, this.data, off, len);
        }
    }

    // Delta interface implementation.

    public long getWriteOffset() {
        return offset;
    }

    public int getBlockLength() {
        return data.length;
    }

    /**
     * Get the offset at which this block should begin.
     *
     * @return The offset at which this block should begin.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Return the array of bytes that is the data block.
     *
     * @return The block itself.
     */
    public byte[] getData() {
        return data.clone();
    }

    /**
     * Return a printable string that represents this object.
     *
     * @return A string representation of this block.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" [ off=").append(offset);
        buf.append(" len=").append(data.length);
        buf.append(" data=");
        final int n = Math.min(data.length, 256);
        for (int i = 0; i < n; i++) {
            if ((data[i] & 0xFF) < 0x10) {
                buf.append('0');
            }
            buf.append(Integer.toHexString(data[i] & 0xFF));
        }
        if (n != data.length) {
            buf.append("...");
        }
        buf.append(" ]");
        return buf.toString();
    }

    /**
     * Return the hash code for this data block.
     *
     * @return The hash code.
     */
    public int hashCode() {
        int b = 0;
        // For fun.
        for (int i = 0; i < data.length; i++) {
            b ^= data[i] << ((i * 8) % 32);
        }
        return b + (int) offset;
    }

    /**
     * Test if another object equals this one.
     *
     * @return <tt>true</tt> If <tt>o</tt> is an instance of DataBlock and
     *         if both the offsets and the byte arrays of both are equal.
     * @throws ClassCastException   If <tt>o</tt> is not an
     *                              instance of this class.
     * @throws NullPointerException If <tt>o</tt> is null.
     */
    public boolean equals(Object o) {
        return offset == ((DataBlock) o).offset &&
                java.util.Arrays.equals(data, ((DataBlock) o).data);
    }
}
