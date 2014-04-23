
package org.metastatic.rsync;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The superclass of objects that encode sets of deltas to external
 * representations, such as the over-the-wire format of rsync or the
 * rdiff file format.
 * <p/>
 * <p>Subclasses MAY define themselves to be accessable through the
 * {@link #getInstance(String, Configuration, java.io.OutputStream)} method
 * by providing a one-argument constructor that accepts an {@link
 * java.io.OutputStream} and defining the system property
 * "rsync.deltaEncoder.<i>encoding-name</i>".
 */
public abstract class DeltaEncoder {

    public static final String PROPERTY = "rsync.deltaEncoder.";

    /**
     * The configuration.
     */
    protected Configuration config;

    /**
     * The output stream.
     */
    protected OutputStream out;

    /**
     * Creates a new delta encoder.
     *
     * @param config The configuration.
     * @param out    The output stream to write the data to.
     */
    public DeltaEncoder(Configuration config, OutputStream out) {
        this.config = config;
        this.out = out;
    }

    /**
     * Returns a new instance of the specified encoder.
     *
     * @throws IllegalArgumentException If there is no appropriate
     *                                  encoder available.
     */
    public static final DeltaEncoder getInstance(String encoding,
                                                 Configuration config,
                                                 OutputStream out) {
        if (encoding == null || config == null || out == null) {
            throw new NullPointerException();
        }
        if (encoding.length() == 0) {
            throw new IllegalArgumentException();
        }
        try {
            Class clazz = Class.forName(System.getProperty(PROPERTY + encoding));
            if (!DeltaEncoder.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(clazz.getName() +
                        ": not a subclass of " +
                        DeltaEncoder.class.getName());
            }
            Constructor c = clazz.getConstructor(new Class[]{Configuration.class,
                    OutputStream.class});
            return (DeltaEncoder) c.newInstance(new Object[]{config, out});
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
     * Write (encode) a list of deltas to the output stream. This method does
     * <b>not</b> call {@link #doFinal()}.
     * <p/>
     * <p>This method checks every element of the supplied list to ensure that
     * all are either non-null or implement the {@link org.metastatic.rsync.Delta} interface, before
     * writing any data.
     *
     * @param deltas The list of deltas to write.
     * @throws java.io.IOException      If an I/O error occurs.
     * @throws IllegalArgumentException If any element of the list is not
     *                                  a {@link org.metastatic.rsync.Delta}.
     * @throws NullPointerException     If any element is null.
     */
    public void write(List<Delta> deltas) throws IOException {
        for (Delta delta : deltas) {
            write(delta);
        }
    }

    /**
     * Write (encode) a single delta to the output stream.
     *
     * @param d The delta to write.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public abstract void write(Delta d) throws IOException;

    /**
     * Finish encoding the deltas (at least, this set of deltas) and write any
     * encoding-specific end-of-deltas entity.
     *
     * @throws java.io.IOException If an I/O error occurs.
     */
    public abstract void doFinal() throws IOException;


}
