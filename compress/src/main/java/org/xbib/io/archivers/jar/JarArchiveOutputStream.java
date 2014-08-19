
package org.xbib.io.archivers.jar;

import org.xbib.io.archivers.zip.JarMarker;
import org.xbib.io.archivers.zip.ZipArchiveOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Subclass that adds a special extra field to the very first entry
 * which allows the created archive to be used as an executable jar on
 * Solaris.
 */
public class JarArchiveOutputStream extends ZipArchiveOutputStream<JarArchiveEntry> {

    private boolean jarMarkerAdded = false;

    public JarArchiveOutputStream(final OutputStream out) {
        super(out);
    }

    @Override
    public void putArchiveEntry(JarArchiveEntry ze) throws IOException {
        if (!jarMarkerAdded) {
            ze.addAsFirstExtraField(JarMarker.getInstance());
            jarMarkerAdded = true;
        }
        super.putArchiveEntry(ze);
    }
}
