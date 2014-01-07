package org.xbib.io.archivers.elasticsearch.bulk;

import org.xbib.io.archivers.ArchiveEntry;
import org.xbib.io.archivers.ArchiveOutputStream;

import java.io.File;
import java.io.IOException;

public class BulkArchiveOutputStream extends ArchiveOutputStream {

    @Override
    public ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
        return null;
    }

    @Override
    public void putArchiveEntry(ArchiveEntry entry) throws IOException {

    }

    @Override
    public void closeArchiveEntry() throws IOException {

    }

    @Override
    public void finish() throws IOException {

    }

}
