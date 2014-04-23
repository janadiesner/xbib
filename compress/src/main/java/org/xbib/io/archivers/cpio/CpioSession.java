
package org.xbib.io.archivers.cpio;

import org.xbib.io.archivers.ArchiveSession;

/**
 * Cpio Session
 */
public class CpioSession extends ArchiveSession<CpioArchiveInputStream, CpioArchiveOutputStream> {

    private final static String cpio = "cpio";

    protected String getSuffix() {
        return cpio;
    }

    public CpioArchiveInputStream getInputStream() {
        return in;
    }

    public CpioArchiveOutputStream getOutputStream() {
        return out;
    }
}
