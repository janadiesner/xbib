
package org.xbib.io.jdbc.pool.bonecp;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;


/**
 * Helper class just for executor service to provide a fancier name for debugging + catch for thread exceptions.
 *
 */
public class CustomThreadFactory
        implements ThreadFactory, UncaughtExceptionHandler {

    /**
     * Daemon state.
     */
    private boolean daemon;
    /**
     * Thread name.
     */
    private String threadName;

    /**
     * Default constructor.
     *
     * @param threadName name for thread.
     * @param daemon     set/unset daemon thread
     */
    public CustomThreadFactory(String threadName, boolean daemon) {
        this.threadName = threadName;
        this.daemon = daemon;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.ThreadFactory#newThread(Runnable)
     */
    //@Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, this.threadName);
        t.setDaemon(this.daemon);
        t.setUncaughtExceptionHandler(this);
        return t;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable)
     */
    public void uncaughtException(Thread thread, Throwable throwable) {
       // logger.error("Uncaught Exception in thread " + thread.getName(), throwable);
    }

}
