package org.xbib.io.archivers.zip;

import org.xbib.io.archivers.ArchiveSession;

public class ZipSession extends ArchiveSession<ZipArchiveInputStream, ZipArchiveOutputStream> {

    private final static String zip = "zip";

    protected String getSuffix() {
        return zip;
    }

    public ZipArchiveInputStream getInputStream() {
        return in;
    }

    public ZipArchiveOutputStream getOutputStream() {
        return out;
    }

}
