package org.apache.commons.fileupload;

import java.io.IOException;

/**
 * An iterator, as returned by
 * {@link org.apache.commons.fileupload.FileUploadBase#getItemIterator(org.apache.commons.fileupload.RequestContext)}.
 *
 */
public interface FileItemIterator {

    /**
     * Returns, whether another instance of {@link org.apache.commons.fileupload.FileItemStream}
     * is available.
     *
     * @throws FileUploadException Parsing or processing the
     *   file item failed.
     * @throws java.io.IOException Reading the file item failed.
     * @return True, if one or more additional file items
     *   are available, otherwise false.
     */
    boolean hasNext() throws FileUploadException, IOException;

    /**
     * Returns the next available {@link org.apache.commons.fileupload.FileItemStream}.
     *
     * @throws java.util.NoSuchElementException No more items are available. Use
     * {@link #hasNext()} to prevent this exception.
     * @throws FileUploadException Parsing or processing the
     *   file item failed.
     * @throws java.io.IOException Reading the file item failed.
     * @return FileItemStream instance, which provides
     *   access to the next file item.
     */
    FileItemStream next() throws FileUploadException, IOException;

}
