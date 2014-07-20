package org.apache.commons.fileupload.util;

import java.io.IOException;

public interface Closeable {

    /**
     * Closes the object.
     *
     * @throws java.io.IOException An I/O error occurred.
     */
    void close() throws IOException;

    /**
     * Returns, whether the object is already closed.
     *
     * @return True, if the object is closed, otherwise false.
     * @throws java.io.IOException An I/O error occurred.
     */
    boolean isClosed() throws IOException;

}
