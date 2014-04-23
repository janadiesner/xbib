
package net.fortuna.ical4j.data;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Arrays;

import net.fortuna.ical4j.util.CompatibilityHints;

/**
 * A reader which performs iCalendar unfolding as it reads. Note that unfolding rules may be "relaxed" to allow
 * unfolding of non-conformant *.ics files. By specifying the system property "ical4j.unfolding.relaxed=true" iCalendar
 * files created with Mozilla Calendar/Sunbird may be correctly unfolded.
 * 
 * To wrap this reader with a {@link java.io.BufferedReader} you must ensure you specify an identical buffer size
 * to that used in the {@link java.io.BufferedReader}.
 */
public class UnfoldingReader extends PushbackReader {

    /**
     * The pattern used to identify a fold in an iCalendar data stream.
     */
    private static final char[] DEFAULT_FOLD_PATTERN_1 = { '\r', '\n', ' ' };
    
    /** 
     * The pattern used to identify a fold in Microsoft Outlook 2007. 
     */ 
    private static final char[] DEFAULT_FOLD_PATTERN_2 = { '\r', '\n', '\t' };

    /**
     * The pattern used to identify a fold in Mozilla Calendar/Sunbird and KOrganizer.
     */
    private static final char[] RELAXED_FOLD_PATTERN_1 = { '\n', ' ' };
    
    /** 
     * The pattern used to identify a fold in Microsoft Outlook 2007. 
     */ 
    private static final char[] RELAXED_FOLD_PATTERN_2 = { '\n', '\t' };
    
    private char[][] patterns;

    private char[][] buffers;

    private int linesUnfolded;
    
    private int maxPatternLength = 0;

    /**
     * Creates a new unfolding reader instance. Relaxed unfolding flag is read from system property.
     * @param in the reader to unfold from
     */
    public UnfoldingReader(final Reader in) {
        this(in, DEFAULT_FOLD_PATTERN_1.length, CompatibilityHints
                .isHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING));
    }
    
    /**
     * @param in reader source for data
     * @param size the buffer size
     */
    public UnfoldingReader(final Reader in, int size) {
        this(in, size, CompatibilityHints.isHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING));
    }

    /**
     * @param in reader source for data
     * @param relaxed indicates whether relaxed unfolding is enabled
     */
    public UnfoldingReader(final Reader in, boolean relaxed) {
        this(in, DEFAULT_FOLD_PATTERN_1.length, relaxed); 
    }

    /**
     * Creates a new unfolding reader instance.
     * @param in a reader to read from
     * @param size the buffer size
     * @param relaxed specifies whether unfolding is relaxed
     */
    public UnfoldingReader(final Reader in, int size, final boolean relaxed) {
        super(in, size);
        if (relaxed) {
            patterns = new char[4][];
            patterns[0] = DEFAULT_FOLD_PATTERN_1;
            patterns[1] = DEFAULT_FOLD_PATTERN_2;
            patterns[2] = RELAXED_FOLD_PATTERN_1;
            patterns[3] = RELAXED_FOLD_PATTERN_2;
        }
        else {
            patterns = new char[2][];
            patterns[0] = DEFAULT_FOLD_PATTERN_1;
            patterns[1] = DEFAULT_FOLD_PATTERN_2;
        }
        buffers = new char[patterns.length][];
        for (int i = 0; i < patterns.length; i++) {
            buffers[i] = new char[patterns[i].length];
            maxPatternLength = Math.max(maxPatternLength, patterns[i].length);
        }
    }

    /**
     * @return number of lines unfolded so far while reading
     */
    public final int getLinesUnfolded() {
        return linesUnfolded;
    }

    /**
     * {@inheritDoc}
     */
    public final int read() throws IOException {
        final int c = super.read();
        boolean doUnfold = false;
        for (int i = 0; i < patterns.length; i++) {
            if (c == patterns[i][0]) {
                doUnfold = true;
                break;
            }
        }
        if (!doUnfold) {
            return c;
        }
        else {
            unread(c);
        }

        unfold();

        return super.read();
    }
    
    /**
     * {@inheritDoc}
     */
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        final int read = super.read(cbuf, off, len);
        boolean doUnfold = false;
        for (int i = 0; i < patterns.length; i++) {
            if (read > 0 && cbuf[0] == patterns[i][0]) {
                doUnfold = true;
                break;
            }
            else {
                for (int j = 0; j < read; j++) {
                    if (cbuf[j] == patterns[i][0]) {
                        unread(cbuf, j, read - j);
                        return j;
                    }
                }
            }
        }
        if (!doUnfold) {
            return read;
        }
        else {
            unread(cbuf, off, read);
        }

        unfold();

        return super.read(cbuf, off, maxPatternLength);
    }
    
    private void unfold() throws IOException {
        // need to loop since one line fold might be directly followed by another
        boolean didUnfold;
        do {
            didUnfold = false;

            for (int i = 0; i < buffers.length; i++) {
                int read = 0;             
                while (read < buffers[i].length) {
                    final int partialRead = super.read(buffers[i], read, buffers[i].length - read);
                    if (partialRead < 0) {
                        break;
                    }
                    read += partialRead;
                }
                if (read > 0) {
                    if (!Arrays.equals(patterns[i], buffers[i])) {
                        unread(buffers[i], 0, read);
                    }
                    else {
                        linesUnfolded++;
                        didUnfold = true;
                    }
                }
            }
        }
        while (didUnfold);
    }
}
