
package org.xbib.template.handlebars.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * Read file content utilities method.
 */
public final class Files {

    /**
     * Not used.
     */
    private Files() {
    }

    /**
     * Read a file from a classpath location.
     *
     * @param location The classpath location.
     * @return The file content.
     * @throws java.io.IOException If the file can't be read.
     */
    public static String read(final String location) throws IOException {
        return read(Files.class.getResourceAsStream(location));
    }

    /**
     * Read a file content.
     *
     * @param source The file.
     * @return The file content.
     * @throws java.io.IOException If the file can't be read.
     */
    public static String read(final File source) throws IOException {
        return read(new FileInputStream(source));
    }

    /**
     * Read a file source.
     *
     * @param source The file source.
     * @return The file content.
     * @throws java.io.IOException If the file can't be read.
     */
    public static String read(final InputStream source) throws IOException {
        return read(new InputStreamReader(source, Charset.forName("UTF-8")));
    }

    /**
     * Read a file source.
     *
     * @param source The file source.
     * @return The file content.
     * @throws java.io.IOException If the file can't be read.
     */
    public static String read(final Reader source) throws IOException {
        return read(new BufferedReader(source));
    }

    /**
     * Read a file source.
     *
     * @param source The file source.
     * @return The file content.
     * @throws java.io.IOException If the file can't be read.
     */
    public static String read(final BufferedReader source) throws IOException {
        notNull(source, "The input is required.");
        try {
            int ch = source.read();
            StringBuilder script = new StringBuilder();
            while (ch != -1) {
                script.append((char) ch);
                ch = source.read();
            }
            return script.toString();
        } finally {
            source.close();
        }
    }
}
