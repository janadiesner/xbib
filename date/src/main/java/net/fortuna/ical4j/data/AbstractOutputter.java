
package net.fortuna.ical4j.data;

import java.nio.charset.Charset;

import net.fortuna.ical4j.util.CompatibilityHints;

/**
 * Base class for model outputters.
 */
public abstract class AbstractOutputter {

    /**
     * The default character set used to generate output.
     */
    protected static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private boolean validating;

    /**
     * The maximum line length allowed.
     */
    protected int foldLength;

    /**
     * Default constructor.
     */
    public AbstractOutputter() {
        this(true);
    }

    /**
     * @param validating indicates whether to validate calendar when outputting to stream
     */
    public AbstractOutputter(final boolean validating) {
        this(validating, CompatibilityHints.isHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY)
                ? FoldingWriter.MAX_FOLD_LENGTH
                        : FoldingWriter.REDUCED_FOLD_LENGTH);
    }

    /**
     * @param validating indicates whether to validate calendar when outputting to stream
     * @param foldLength maximum number of characters before a line is folded
     */
    public AbstractOutputter(final boolean validating, final int foldLength) {
        this.validating = validating;
        this.foldLength = foldLength;
    }

    /**
     * @return Returns the validating.
     */
    public final boolean isValidating() {
        return validating;
    }

    /**
     * @param validating The validating to set.
     */
    public final void setValidating(final boolean validating) {
        this.validating = validating;
    }
}
