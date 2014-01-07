package org.xbib.io.archivers.jar;

import org.xbib.io.archivers.ArchiveSession;

public class JarSession extends ArchiveSession<JarArchiveInputStream, JarArchiveOutputStream> {

    private final static String jar = "jar";

    protected String getSuffix() {
        return jar;
    }

    public JarArchiveInputStream getInputStream() {
        return in;
    }

    public JarArchiveOutputStream getOutputStream() {
        return out;
    }

}
