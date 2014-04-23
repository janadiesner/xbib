
package net.fortuna.ical4j.data;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * A writer that performs iCalendar folding as it writes.
 */
public class FoldingWriter extends FilterWriter {

    /**
     * reduced to 73 to be consistent with Apple iCal..
     */
    public static final int REDUCED_FOLD_LENGTH = 73;

    /**
     * Lines of text SHOULD NOT be longer than 75 octets, excluding the line break.
     */
    public static final int MAX_FOLD_LENGTH = 75;

    private static final char[] FOLD_PATTERN = { '\r', '\n', ' ' };

    private int lineLength;

    private final int foldLength;

    /**
     * @param writer a writer to write output to
     * @param foldLength the maximum line length
     */
    public FoldingWriter(final Writer writer, final int foldLength) {
        super(writer);
        this.foldLength = Math.min(foldLength, MAX_FOLD_LENGTH);
    }

    /**
     * @param writer a writer to write output to
     */
    public FoldingWriter(final Writer writer) {
        this(writer, REDUCED_FOLD_LENGTH);
    }

    public final void write(final int c) throws IOException {

        write(new char[] { (char) c }, 0, 1);
    }

    public final void write(final char[] buffer, final int offset,
            final int length) throws IOException {
        final int maxIndex = offset + length - 1;
        for (int i = offset; i <= maxIndex; i++) {

            // check for fold first so we don't unnecessarily fold after
            // no more data..
            if (lineLength >= foldLength) {
                super.write(FOLD_PATTERN, 0, FOLD_PATTERN.length);

                // re-initialise to 1 to account for the space in fold pattern..
                lineLength = 1;
            }

            super.write(buffer[i]);

            if (buffer[i] == '\r' || buffer[i] == '\n') {
                lineLength = 0;
            }
            else {
                lineLength += 1;
            }
        }
    }

    public final void write(final String str, final int off, final int len)
            throws IOException {
        write(str.toCharArray(), off, len);
    }

}
