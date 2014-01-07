
package org.xbib.io.archivers.jar;

import org.xbib.io.archivers.ArchiveEntry;
import org.xbib.io.archivers.zip.ZipArchiveEntry;
import org.xbib.io.archivers.zip.ZipArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implements an input stream that can read entries from jar files.
 */
public class JarArchiveInputStream extends ZipArchiveInputStream {

    public JarArchiveInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    public JarArchiveEntry getNextJarEntry() throws IOException {
        ZipArchiveEntry entry = getNextZipEntry();
        return entry == null ? null : new JarArchiveEntry(entry);
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return getNextJarEntry();
    }

    /**
     * Checks if the signature matches what is expected for a jar file
     * (in this case it is the same as for a zip file).
     *
     * @param signature the bytes to check
     * @param length    the number of bytes to check
     * @return true, if this stream is a jar archive stream, false otherwise
     */
    public static boolean matches(byte[] signature, int length) {
        return ZipArchiveInputStream.matches(signature, length);
    }
}
