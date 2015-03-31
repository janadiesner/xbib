package org.semarglproject.sink;

import java.io.IOException;

/**
 * Interface for handling events from {@link org.semarglproject.source.CharSource}
 */
public interface CharSink extends Sink {

    /**
     * Callback for string processing
     *
     * @param str string for processing
     * @throws IOException
     */
    CharSink process(String str) throws IOException;

    /**
     * Callback for char processing
     *
     * @param ch char for processing
     * @throws IOException
     */
    CharSink process(char ch) throws IOException;

    /**
     * Callback for buffer processing
     *
     * @param buffer char buffer for processing
     * @param start  position to start
     * @param count  count of chars to process
     * @throws IOException
     */
    CharSink process(char[] buffer, int start, int count) throws IOException;
}
