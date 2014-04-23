
package org.xbib.template.handlebars.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import static org.xbib.template.handlebars.util.Validate.notEmpty;
import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * An {@link java.net.URL} {@link org.xbib.template.handlebars.io.TemplateSource}.
 */
public class URLTemplateSource extends AbstractTemplateSource {

    /**
     * The resource. Required.
     */
    private URL resource;

    /**
     * The last modified date.
     */
    private long lastModified;

    /**
     * The file's name.
     */
    private String filename;

    /**
     * Creates a new {@link org.xbib.template.handlebars.io.URLTemplateSource}.
     *
     * @param filename The file's name.
     * @param resource The resource. Required.
     */
    public URLTemplateSource(final String filename, final URL resource) {
        this.filename = notEmpty(filename, "The filename is required.");
        this.resource = notNull(resource, "A resource is required.");
        this.lastModified = lastModified(resource);
    }

    @Override
    public String content() throws IOException {
        Reader reader = null;
        final int bufferSize = 1024;
        try {
            reader = reader();
            char[] cbuf = new char[bufferSize];
            StringBuilder sb = new StringBuilder(bufferSize);
            int len;
            while ((len = reader.read(cbuf, 0, bufferSize)) != -1) {
                sb.append(cbuf, 0, len);
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    public String filename() {
        return filename;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public Reader reader() throws IOException {
        InputStream in = resource.openStream();
        return new InputStreamReader(in, "UTF-8");
    }

    /**
     * Read the last modified date from a resource.
     *
     * @param resource The resource.
     * @return The last modified date from a resource.
     */
    private long lastModified(final URL resource) {
        URLConnection uc = null;
        try {
            uc = resource.openConnection();
            return uc.getLastModified();
        } catch (IOException ex) {
            return -1;
        } finally {
            try {
                if (uc != null) {
                    InputStream is = uc.getInputStream();
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException e) {
            }
        }
    }

}
