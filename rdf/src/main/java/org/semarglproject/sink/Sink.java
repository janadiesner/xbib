package org.semarglproject.sink;

import java.io.IOException;

/**
 * Base sink interface.
 */
public interface Sink {

    /**
     * Sets document base URI. Must be called befor start stream event.
     *
     * @param baseUri base URI
     */
    void setBaseUri(String baseUri);

    /**
     * Callback for start stream event.
     *
     * @throws IOException
     */
    void startStream() throws IOException;

    /**
     * Callback for end stream event.
     *
     * @throws IOException
     */
    void endStream() throws IOException;

}
