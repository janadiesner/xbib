
package org.xbib.io.jdbc.pool.bonecp.listener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *  Class passed to onAcquireFail
 */
public class AcquireFailConfig {
    /**
     * Delay to use between one acquire retry attempt and the next.
     */
    private long acquireRetryDelayInMs;
    /**
     * Number of attempts left before giving up.
     */
    private AtomicInteger acquireRetryAttempts = new AtomicInteger();
    /**
     * Message that shows the origin of the problem.
     */
    private String logMessage = "";

    /**
     * Deprecated. Use {@link #getAcquireRetryDelayInMs()} instead.
     *
     * @return the acquireRetryDelay value
     * @deprecated Use {@link #getAcquireRetryDelayInMs()} instead.
     */
    @Deprecated
    public long getAcquireRetryDelay() {
        return getAcquireRetryDelayInMs();
    }

    /**
     * Getter for acquireRetryDelay. By default starts off with whatever is set in the config.
     *
     * @return the acquireRetryDelay value
     */
    public long getAcquireRetryDelayInMs() {
        return this.acquireRetryDelayInMs;
    }

    /**
     * Deprecated. Use {@link #setAcquireRetryDelayInMs(long)} instead.
     *
     * @param acquireRetryDelayInMs the acquireRetryDelay to set
     * @deprecated Use {@link #setAcquireRetryDelayInMs(long)} instead.
     */
    @Deprecated
    public void setAcquireRetryDelay(long acquireRetryDelayInMs) {
        setAcquireRetryDelayInMs(acquireRetryDelayInMs);
    }

    /**
     * Sets the new acquireRetryDelay. Does not affect the global config.
     *
     * @param acquireRetryDelayInMs the acquireRetryDelay to set
     */
    public void setAcquireRetryDelayInMs(long acquireRetryDelayInMs) {
        this.acquireRetryDelayInMs = acquireRetryDelayInMs;
    }

    /**
     * Returns the acquireRetryAttemps. By default starts off with whatever is set in the config.
     *
     * @return the acquireRetryAttempts value.
     */
    public AtomicInteger getAcquireRetryAttempts() {
        return this.acquireRetryAttempts;
    }

    /**
     * Sets the new acquireRetyAttemps.
     *
     * @param acquireRetryAttempts the acquireRetryAttempts to set
     */
    public void setAcquireRetryAttempts(AtomicInteger acquireRetryAttempts) {
        this.acquireRetryAttempts = acquireRetryAttempts;
    }

    /**
     * Returns a message that shows the origin of the problem.
     *
     * @return the logMessage to display
     */
    public String getLogMessage() {
        return this.logMessage;
    }

    /**
     * Sets a log message.
     *
     * @param logMessage the logMessage to set
     */
    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

}
