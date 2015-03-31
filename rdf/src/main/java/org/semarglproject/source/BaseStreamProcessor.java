package org.semarglproject.source;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Pipeline managing class to subclass from.
 */
public abstract class BaseStreamProcessor {

    protected abstract void startStream() throws IOException;

    protected abstract void endStream() throws IOException;

    protected abstract void processInternal(Reader reader, String mimeType, String baseUri) throws IOException;

    protected abstract void processInternal(InputStream inputStream, String mimeType,
                                            String baseUri) throws IOException;

    /**
     * Processes specified document's file using file path as base URI
     *
     * @param file document's file
     * @throws org.semarglproject.rdf.IOException
     */
    public final void process(File file) throws IOException {
        String baseUri = "file://" + file.getAbsolutePath();
        process(file, baseUri);
    }

    /**
     * Processes specified document's file
     *
     * @param file    document's file
     * @param baseUri document's URI
     * @throws org.semarglproject.rdf.IOException
     */
    public final void process(File file, String baseUri) throws IOException {
        FileReader reader;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }
        try {
            process(reader, null, baseUri);
        } finally {
            closeQuietly(reader);
        }
    }

    /**
     * Processes document pointed by specified URI
     *
     * @param uri document's URI
     * @throws org.semarglproject.rdf.IOException
     */
    public final void process(String uri) throws IOException {
        process(uri, uri);
    }

    /**
     * Processes document pointed by specified URI. Uses specified URI as document's base.
     *
     * @param uri     document's URI
     * @param baseUri document's URI
     * @throws org.semarglproject.rdf.IOException
     */
    public final void process(String uri, String baseUri) throws IOException {
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            throw new IOException(e);
        }
        try {
            URLConnection urlConnection = url.openConnection();
            String mimeType = urlConnection.getContentType();
            InputStream inputStream = urlConnection.getInputStream();
            try {
                process(inputStream, mimeType, baseUri);
            } finally {
                closeQuietly(inputStream);
            }
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Processes stream input for document
     *
     * @param inputStream document's input stream
     * @param baseUri     document's base URI
     * @throws org.semarglproject.rdf.IOException
     */
    public void process(InputStream inputStream, String baseUri) throws IOException {
        process(inputStream, null, baseUri);
    }

    /**
     * Processes stream input for document
     *
     * @param inputStream document's input stream
     * @param mimeType    document's MIME type
     * @param baseUri     document's base URI
     * @throws org.semarglproject.rdf.IOException
     */
    public final void process(InputStream inputStream, String mimeType, String baseUri) throws IOException {
        startStream();
        try {
            processInternal(inputStream, mimeType, baseUri);
        } finally {
            endStream();
        }
    }

    /**
     * Processes reader input for document's
     *
     * @param reader document's reader
     * @throws IOException
     */
    public void process(Reader reader, String baseUri) throws IOException {
        process(reader, null, baseUri);
    }

    /**
     * Processes reader input for document's
     *
     * @param reader   document's reader
     * @param mimeType document's MIME type
     * @param baseUri  document's base URI
     * @throws IOException
     */
    public final void process(Reader reader, String mimeType, String baseUri) throws IOException {
        startStream();
        try {
            processInternal(reader, mimeType, baseUri);
        } finally {
            endStream();
        }
    }


    static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (java.io.IOException ioe) {
            // ignore
        }
    }

}
