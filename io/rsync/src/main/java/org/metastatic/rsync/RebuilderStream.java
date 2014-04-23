
package org.metastatic.rsync;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

/**
 * To use this class,
 * create an instance with a file argument representing the file being
 * rebuilt. Then register one or more implementations of the {@link
 * RebuilderListener} interface, which will write the data to the new
 * file. Then call the {@link update(Delta)} method for each {@link
 * org.metastatic.rsync.Delta} to be applied.
 * <p/>
 * <p>Note that unlike the {@link org.metastatic.rsync.GeneratorStream} and {@link
 * org.metastatic.rsync.MatcherStream} classes this class does not need a {@link
 * org.metastatic.rsync.Configuration}, nor does it have any "doFinal" method -- it is
 * completely stateless (except for the file) and the operations are
 * finished when the last delta has been applied.
 * <p/>
 * <p>This class is optimal for situations where the deltas are coming
 * in a stream over a communications link, and when it would be
 * inefficient to wait until all deltas are received.
 */
public class RebuilderStream {

    /**
     * The basis file.
     */
    protected RandomAccessFile basisFile;

    /**
     * The list of {@link RebuilderListener}s.
     */
    protected final LinkedList<RebuilderListener> listeners;

    /**
     * Create a new rebuilder.
     */
    public RebuilderStream() {
        listeners = new LinkedList<RebuilderListener>();
    }

    /**
     * Add a RebuilderListener listener to this rebuilder.
     *
     * @param listener The listener to add.
     * @throws IllegalArgumentException If <i>listener</i> is null.
     */
    public void addListener(RebuilderListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        listeners.add(listener);
    }

    /**
     * Set the basis file.
     *
     * @param file The basis file.
     * @throws java.io.IOException If the file is not readable.
     */
    public void setBasisFile(File file) throws IOException {
        if (basisFile != null) {
            basisFile.close();
            basisFile = null;
        }
        if (file != null) {
            basisFile = new RandomAccessFile(file, "r");
        }
    }

    /**
     * Set the basis file.
     *
     * @param file The basis file name.
     * @throws java.io.IOException If the file name is not the name of a readable file.
     */
    public void setBasisFile(String file) throws IOException {
        if (basisFile != null) {
            basisFile.close();
            basisFile = null;
        }
        if (file != null) {
            basisFile = new RandomAccessFile(file, "r");
        }
    }

    /**
     *
     */
    public void doFinal() throws IOException {
        if (basisFile != null) {
            basisFile.close();
        }
    }

    /**
     * Update this rebuilder with a delta.
     *
     * @param delta The delta to apply.
     * @throws IOException If there is an error reading from the basis
     *                             file, or if no basis file has been specified.
     * @throws ListenerException
     */
    public void update(Delta delta) throws IOException, ListenerException {
        ListenerException exception = null, current = null;
        RebuilderEvent e = null;
        if (delta instanceof DataBlock) {
            e = new RebuilderEvent(((DataBlock) delta).getData(),
                    delta.getWriteOffset());
        } else {
            if (basisFile == null) {
                throw new IOException("offsets found but no basis file specified");
            }
            int len = Math.min(delta.getBlockLength(),
                    (int) (basisFile.length() - ((Offsets) delta).getOldOffset()));
            if (len < 0) {
                return;
            }
            byte[] buf = new byte[len];
            basisFile.seek(((Offsets) delta).getOldOffset());
            len = basisFile.read(buf);
            e = new RebuilderEvent(buf, 0, len, delta.getWriteOffset());
        }
        for (RebuilderListener l : listeners) {
            try {
                l.update(e);
            } catch (ListenerException le) {
                if (exception != null) {
                    current.setNext(le);
                    current = le;
                } else {
                    exception = le;
                    current = le;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}
