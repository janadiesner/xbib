
package org.xbib.io.archivers.ar;

import org.xbib.io.archivers.ArchiveSession;

/**
 * Ar Session
 */
public class ArSession extends ArchiveSession<ArArchiveInputStream, ArArchiveOutputStream> {

    private final static String ar = "ar";

    protected String getSuffix() {
        return ar;
    }

    public ArArchiveInputStream getInputStream() {
        return in;
    }

    public ArArchiveOutputStream getOutputStream() {
        return out;
    }
}
