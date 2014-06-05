package org.xbib.util;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates UUIDs according to the DCE Universal Token Identifier specification.
 * <p>
 * All you need to know:
 * <pre>
 * UUID u = new UUID();
 * </pre>
 */
public class UUID implements Comparable<UUID> {

    /**
     * The time field of the UUID.
     */
    private long time;

    /**
     * The clock sequence and node field of the UUID.
     */
    private long clockSeqAndNode;

    public UUID() {
        this(newTime(), MAC.getClockSeqAndNode());
    }

    /**
     * Constructor for UUID. Constructs a UUID from two <code>long</code> values.
     *
     * @param time            the upper 64 bits
     * @param clockSeqAndNode the lower 64 bits
     */
    public UUID(long time, long clockSeqAndNode) {
        this.time = time;
        this.clockSeqAndNode = clockSeqAndNode;
    }

    /**
     * Copy constructor for UUID. Values of the given UUID are copied.
     *
     * @param u the UUID, may not be <code>null</code>
     */
    public UUID(UUID u) {
        this(u.time, u.clockSeqAndNode);
    }

    /**
     * Parses a textual representation of a UUID.
     * <p>
     * No validation is performed. If the {@link CharSequence} is shorter than 36 characters,
     * {@link ArrayIndexOutOfBoundsException}s will be thrown.
     *
     * @param s the {@link CharSequence}, may not be <code>null</code>
     */
    public UUID(CharSequence s) {
        this(Hex.parseLong(s.subSequence(0, 18)), Hex.parseLong(s.subSequence(19, 36)));
    }

    /**
     * Compares this UUID to another Object. Throws a {@link ClassCastException} if
     * the other Object is not an instance of the UUID class. Returns a value
     * smaller than zero if the other UUID is "larger" than this UUID and a value
     * larger than zero if the other UUID is "smaller" than this UUID.
     *
     * @param t the other UUID, may not be <code>null</code>
     * @return a value &lt; 0, 0 or a value &gt; 0
     * @throws ClassCastException
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(UUID t) {
        if (this == t) {
            return 0;
        }
        if (time > t.time) {
            return 1;
        }
        if (time < t.time) {
            return -1;
        }
        if (clockSeqAndNode > t.clockSeqAndNode) {
            return 1;
        }
        if (clockSeqAndNode < t.clockSeqAndNode) {
            return -1;
        }
        return 0;
    }

    /**
     * Returns this UUID as a String.
     *
     * @return a String, never <code>null</code>
     * @see Object#toString()
     * @see #toAppendable(Appendable)
     */
    @Override
    public final String toString() {
        return toAppendable(null).toString();
    }

    /**
     * Appends a String representation of this object to the given {@link Appendable} object.
     * <p>
     * For reasons I'll probably never understand, Sun has decided to have a number of I/O classes implement
     * Appendable which forced them to destroy an otherwise nice and simple interface with {@link java.io.IOException}s.
     * <p>
     * I decided to ignore any possible IOExceptions in this method.
     *
     * @param a the Appendable object, may be <code>null</code>
     * @return an Appendable object, defaults to a {@link StringBuilder} if <code>a</code> is <code>null</code>
     */
    public Appendable toAppendable(Appendable a) {
        Appendable out = a;
        if (out == null) {
            out = new StringBuilder(36);
        }
        try {
            Hex.append(out, (int) (time >> 32)).append('-');
            Hex.append(out, (short) (time >> 16)).append('-');
            Hex.append(out, (short) time).append('-');
            Hex.append(out, (short) (clockSeqAndNode >> 48)).append('-');
            Hex.append(out, clockSeqAndNode, 12);
        } catch (IOException ex) {
            // What were they thinking?
        }
        return out;
    }

    /**
     * Returns a hash code of this UUID. The hash code is calculated by XOR'ing the
     * upper 32 bits of the time and clockSeqAndNode fields and the lower 32 bits of
     * the time and clockSeqAndNode fields.
     *
     * @return an <code>int</code> representing the hash code
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) ((time >> 32) ^ time ^ (clockSeqAndNode >> 32) ^ clockSeqAndNode);
    }


    /**
     * Returns the time field of the UUID (upper 64 bits).
     *
     * @return the time field
     */
    public final long getTime() {
        return time;
    }

    /**
     * Returns the clock and node field of the UUID (lower 64 bits).
     *
     * @return the clockSeqAndNode field
     */
    public final long getClockSeqAndNode() {
        return clockSeqAndNode;
    }

    /**
     * Compares two Objects for equality.
     *
     * @param obj the Object to compare this UUID with, may be <code>null</code>
     * @return <code>true</code> if the other Object is equal to this UUID,
     * <code>false</code> if not
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UUID)) {
            return false;
        }
        return compareTo((UUID) obj) == 0;
    }

    /**
     * Returns the nil UUID (a UUID whose values are both set to zero).
     * <p>
     * Starting with version 2.0, this method does return a new UUID instance every
     * time it is called. Earlier versions returned one instance. This has now been
     * changed because this UUID has public, non-final instance fields. Returning a
     * new instance is therefore more safe.
     *
     * @return a nil UUID, never <code>null</code>
     */
    public static UUID nilUUID() {
        return new UUID(0, 0);
    }

    /**
     * The last time value. Used to remove duplicate UUIDs.
     */
    private static AtomicLong lastTime = new AtomicLong(Long.MIN_VALUE);

    /**
     * Generates a new time field. Each time field is unique and larger than the
     * previously generated time field.
     *
     * @return a new time value
     */
    public static long newTime() {
        return createTime(System.currentTimeMillis());
    }

    /**
     * Creates a new time field from the given timestamp. Note that even identical
     * values of <code>currentTimeMillis</code> will produce different time fields.
     *
     * @param currentTimeMillis the timestamp
     * @return a new time value
     */
    public static long createTime(long currentTimeMillis) {
        long time;
        // UTC time
        long timeMillis = (currentTimeMillis * 10000) + 0x01B21DD213814000L;
        while (true) {
            long current = lastTime.get();
            if (timeMillis > current) {
                if (lastTime.compareAndSet(current, timeMillis)) {
                    break;
                }
            } else {
                if (lastTime.compareAndSet(current, current + 1)) {
                    timeMillis = current + 1;
                    break;
                }
            }
        }
        // time low
        time = timeMillis << 32;
        // time mid
        time |= (timeMillis & 0xFFFF00000000L) >> 16;
        // time hi and version
        time |= 0x1000 | ((timeMillis >> 48) & 0x0FFF); // version 1
        return time;
    }
}
