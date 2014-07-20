package org.apache.commons.fileupload;

/**
 * Interface that will indicate that {@link org.apache.commons.fileupload.FileItem} or {@link org.apache.commons.fileupload.FileItemStream}
 * implementations will accept the headers read for the item.
 *
 */
public interface FileItemHeadersSupport {

    /**
     * Returns the collection of headers defined locally within this item.
     *
     * @return the {@link org.apache.commons.fileupload.FileItemHeaders} present for this item.
     */
    FileItemHeaders getHeaders();

    /**
     * Sets the headers read from within an item.  Implementations of
     * {@link org.apache.commons.fileupload.FileItem} or {@link org.apache.commons.fileupload.FileItemStream} should implement this
     * interface to be able to get the raw headers found within the item
     * header block.
     *
     * @param headers the instance that holds onto the headers
     *         for this instance.
     */
    void setHeaders(FileItemHeaders headers);

}
