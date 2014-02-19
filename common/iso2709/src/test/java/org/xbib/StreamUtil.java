package org.xbib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public final class StreamUtil {

    public final static int BUFSIZE = 8192;

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFSIZE];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    public static void copy(Reader reader, Writer writer) throws IOException {
        char[] buffer = new char[BUFSIZE];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, len);
        }
    }

}
