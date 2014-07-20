package org.asynchttpclient;

import java.io.IOException;

public interface ResumableBodyConsumer extends BodyConsumer {

    /**
     * Prepare this consumer to resume a download, for example by seeking to the end of the underlying file.
     *
     * @throws java.io.IOException
     */
    void resume() throws IOException;

    /**
     * Get the previously transferred bytes, for example the current file size.
     *
     * @throws java.io.IOException
     */
    long getTransferredBytes() throws IOException;


}
