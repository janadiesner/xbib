
package org.xbib.io.archivers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility functions
 */
public final class IOUtils {

    private final static int BUFSIZE = 4096;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private IOUtils() {
    }

    /**
     * Copies the content of a InputStream into an OutputStream.
     *
     * @param input  the InputStream to copy
     * @param output the target Stream
     * @throws java.io.IOException if an error occurs
     */
    public static long copy(final InputStream input, final OutputStream output) throws IOException {
        return copy(input, output, BUFSIZE);
    }

    /**
     * Copies the content of a InputStream into an OutputStream over the heap
     *
     * @param input      the InputStream to copy
     * @param output     the target Stream
     * @param buffersize the buffer size to use
     * @throws java.io.IOException if an error occurs
     */
    public static long copy(final InputStream input, final OutputStream output, int buffersize) throws IOException {
        final byte[] buffer = new byte[buffersize];
        int n;
        long count = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
