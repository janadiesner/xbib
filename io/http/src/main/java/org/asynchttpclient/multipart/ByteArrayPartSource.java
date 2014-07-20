package org.asynchttpclient.multipart;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is an adaptation of the Apache HttpClient implementation
 *
 * @link http://hc.apache.org/httpclient-3.x/
 */
public class ByteArrayPartSource implements PartSource {

    /**
     * Name of the source file.
     */
    private final String fileName;

    /**
     * Byte array of the source file.
     */
    private final byte[] bytes;

    /**
     * Constructor for ByteArrayPartSource.
     *
     * @param fileName the name of the file these bytes represent
     * @param bytes    the content of this part
     */
    public ByteArrayPartSource(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }

    /**
     * @see org.asynchttpclient.multipart.PartSource#getLength()
     */
    public long getLength() {
        return bytes.length;
    }

    /**
     * @see org.asynchttpclient.multipart.PartSource#getFileName()
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @see org.asynchttpclient.multipart.PartSource#createInputStream()
     */
    public InputStream createInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }

}
