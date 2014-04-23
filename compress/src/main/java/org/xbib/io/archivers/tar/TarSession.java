package org.xbib.io.archivers.tar;

import org.xbib.io.archivers.ArchiveSession;

public class TarSession extends ArchiveSession<TarArchiveInputStream, TarArchiveOutputStream> {

    private final static String tar = "tar";

    protected String getSuffix() {
        return tar;
    }

    public TarArchiveInputStream getInputStream() {
        return in;
    }

    public TarArchiveOutputStream getOutputStream() {
        return out;
    }

}
