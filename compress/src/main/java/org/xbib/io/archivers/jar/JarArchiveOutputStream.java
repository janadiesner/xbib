
package org.xbib.io.archivers.jar;

import org.xbib.io.archivers.ArchiveEntry;
import org.xbib.io.archivers.zip.JarMarker;
import org.xbib.io.archivers.zip.ZipArchiveEntry;
import org.xbib.io.archivers.zip.ZipArchiveOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Subclass that adds a special extra field to the very first entry
 * which allows the created archive to be used as an executable jar on
 * Solaris.
 */
public class JarArchiveOutputStream extends ZipArchiveOutputStream {

    private boolean jarMarkerAdded = false;

    public JarArchiveOutputStream(final OutputStream out) {
        super(out);
    }

    // @throws ClassCastException if entry is not an instance of ZipArchiveEntry
    @Override
    public void putArchiveEntry(ArchiveEntry ze) throws IOException {
        if (!jarMarkerAdded) {
            ((ZipArchiveEntry) ze).addAsFirstExtraField(JarMarker.getInstance());
            jarMarkerAdded = true;
        }
        super.putArchiveEntry(ze);
    }
}
