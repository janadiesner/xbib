package org.snmp4j.util;

/**
 * This models a <code>WorkerTask</code> instance that would be executed by a
 * {@link org.snmp4j.util.WorkerPool} upon submission.
 */
public interface WorkerTask extends Runnable {

    /**
     * The <code>WorkerPool</code> might call this method to hint the active
     * <code>WorkTask</code> instance to complete execution as soon as possible.
     */
    void terminate();

    /**
     * Waits until this task has been finished.
     */
    void join() throws InterruptedException;

    /**
     * Interrupts this task.
     *
     * @see Thread#interrupt()
     */
    void interrupt();

}
