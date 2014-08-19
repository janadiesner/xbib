package org.xbib.io.archive.elasticsearch.bulk;

import org.xbib.io.archivers.ArchiveOutputStream;

import java.io.File;
import java.io.IOException;

public class BulkArchiveOutputStream extends ArchiveOutputStream<BulkArchiveEntry> {

    @Override
    public BulkArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
        return null;
    }

    @Override
    public BulkArchiveEntry newArchiveEntry() throws IOException {
        return new BulkArchiveEntry();
    }

    @Override
    public void putArchiveEntry(BulkArchiveEntry entry) throws IOException {

    }

    @Override
    public void closeArchiveEntry() throws IOException {

    }

    @Override
    public void finish() throws IOException {

    }

}
