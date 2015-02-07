package org.xbib.trie.ahocorasick;

public class Result {
    /**
     * The payload associated with the located substring.
     */
    private Object output;

    /**
     * The index (included) in the whole string where the located substring
     * starts.
     */
    private int startIndex;

    /**
     * The index (excluded) in the whole string where the located substring
     * ends.
     */
    private int lastIndex;

    /**
     * It creates a new Result with the output and the last index passed
     * as parameter. It set the startIndex attribute only if the output object
     * is a String.
     */
    public Result(Object output, int startIndex, int lastIndex) {
        this.output = output;
        this.startIndex = startIndex;
        this.lastIndex = lastIndex;
    }

    public Object getOutput() {
        return output;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    /**
     * An output with span <code>(start1,last1)</code> overlaps an output with
     * span <code>(start2,last2)</code> if and only if either end points of the
     * second output lie within the first chunk:
     * <ul>
     * <li> <code>start1 <= start2 < last1</code>, or
     * <li> <code>start1 < last2 <= last1</code>.
     * </ul>
     */
    public boolean isOverlapped(Result other) {
        return (this.startIndex <= other.startIndex && other.startIndex < this.lastIndex)
                || (this.startIndex < other.lastIndex && other.lastIndex <= this.lastIndex);
    }

    /**
     * An output <code>output1=(start1,last1)</code> dominates another output
     * <code>output2=(start2,last2)</code> if and only if the outputs overlap
     * and:
     * <p/>
     * <ul>
     * <li> <code>start1 &lt; start2</code> (leftmost), or
     * <li> <code>start1 == start2</code> and <code>last1 &gt; last2</code>
     * (longest).
     * </ul>
     */
    public boolean dominate(Result other) {
        return isOverlapped(other)
                && ((this.startIndex < other.startIndex) || (this.startIndex == other.startIndex && this.lastIndex > other.lastIndex));
    }

    public String toString() {
        return "[" + getStartIndex() + "," + getLastIndex() + "]: " + getOutput();
    }
}
