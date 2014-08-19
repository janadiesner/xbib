package org.xbib.io.archivers.tar2;

import org.xbib.io.archivers.ArchiveSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TarSession extends ArchiveSession<TarArchiveInputStream, TarArchiveOutputStream> {

    private TarArchiveInputStream in;

    private TarArchiveOutputStream out;

    protected String getSuffix() {
        return TarConnectionFactory.SUFFIX;
    }

    protected void open(InputStream in) throws IOException {
        this.in = new TarArchiveInputStream(in);
        //this.in = new TarArchiveReadableByteChannel(Channels.newChannel(in));
    }

    protected void open(OutputStream out) {
        this.out = new TarArchiveOutputStream(out);
    }

    public TarArchiveInputStream getInputStream() {
        return in;
    }

    public TarArchiveOutputStream getOutputStream() {
        return out;
    }

}
