
package org.metastatic.rsync;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The superclass of all classes that decode delta objects from an
 * external, binary format.
 * <p/>
 * <p>Subclasses MAY define themselves to be accessable through the
 * {@link #getInstance(String, Configuration, java.io.InputStream)} method
 * by providing a one-argument constructor that accepts an {@link
 * java.io.InputStream} and defining the system property
 * "jarsync.deltaDecoder.<i>encoding-name</i>".
 */
public abstract class DeltaDecoder {


    public static final String PROPERTY = "rsync.deltaDecoder.";

    /**
     * The configuration.
     */
    protected final Configuration config;

    /**
     * The underlying input stream.
     */
    protected final InputStream in;

    // Constructors.
    // -------------------------------------------------------------------------

    public DeltaDecoder(Configuration config, InputStream in) {
        this.config = config;
        this.in = in;
    }

    // Class methods.
    // -------------------------------------------------------------------------

    /**
     * Returns a new instance of the specified decoder.
     *
     * @param encoding The name of the decoder to get.
     * @param config   The configuration to use.
     * @param in       The source of binary data.
     * @return The new decoder.
     * @throws NullPointerException     If any parameter is null.
     * @throws IllegalArgumentException If there is no appropriate
     *                                  decoder available.
     */
    public static final DeltaDecoder getInstance(String encoding,
                                                 Configuration config,
                                                 InputStream in) {
        if (encoding == null || config == null || in == null) {
            throw new NullPointerException();
        }
        if (encoding.length() == 0) {
            throw new IllegalArgumentException();
        }
        try {
            Class clazz = Class.forName(System.getProperty(PROPERTY + encoding));
            if (!DeltaDecoder.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(clazz.getName() +
                        ": not a subclass of " +
                        DeltaDecoder.class.getName());
            }
            Constructor c = clazz.getConstructor(new Class[]{Configuration.class,
                    InputStream.class});
            return (DeltaDecoder) c.newInstance(new Object[]{in});
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("class not found: " +
                    cnfe.getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new IllegalArgumentException("subclass has no constructor");
        } catch (InvocationTargetException ite) {
            throw new IllegalArgumentException(ite.getMessage());
        } catch (InstantiationException ie) {
            throw new IllegalArgumentException(ie.getMessage());
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(iae.getMessage());
        }
    }

    /**
     * Read (decode) a list of deltas from the input stream.
     *
     * @param deltas The list of deltas to write.
     * @throws java.io.IOException      If an I/O error occurs.
     * @throws IllegalArgumentException If any element of the list is not
     *                                  a {@link org.metastatic.rsync.Delta}.
     * @throws NullPointerException     If any element is null.
     */
    public int read(List<Delta> deltas) throws IOException {
        int count = 0;
        Delta d = null;
        while ((d = read()) != null) {
            deltas.add(d);
            ++count;
        }
        return count;
    }

    /**
     * Read (decode) a single delta from the input stream.
     * <p/>
     * <p>If this encoding provides an end-of-deltas marker, then this method
     * is required to return <code>null</code> upon receiving this marker.
     *
     * @return The delta read, or <code>null</code>
     * @throws java.io.IOException If an I/O error occurs.
     */
    public abstract Delta read() throws IOException;
}
