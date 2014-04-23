
package org.xbib.io.archivers.dump;


/**
 * Short File Exception. There was an unexpected EOF when reading
 * the input stream.
 */
public class ShortFileException extends DumpArchiveException {
    private static final long serialVersionUID = 1L;

    public ShortFileException() {
        super("unexpected EOF");
    }
}
