package org.snmp4j.util;

/**
 * The <code>ThreadFactory</code> describes a factory for threads of execution
 * modeled as <code>WorkerTask</code>s.
 */
public interface ThreadFactory {

    /**
     * Creates a new thread of execution for the supplied task. The returned
     * <code>WorkerTask</code> is a symetric wrapper for the supplied one.
     * When the returned task is being run, the supplied one will be executed
     * in a new thread of execution until it either terminates or the
     * {@link org.snmp4j.util.WorkerTask#terminate()} method has been called.
     *
     * @param name   the name of the execution thread.
     * @param task   the task to be executed in the new thread.
     * @param daemon indicates whether the new thread is a daemon (<code>true</code> or an
     *               user thread (<code>false</code>).
     * @return the <code>WorkerTask</code> wrapper to control start and termination of
     * the thread.
     */
    WorkerTask createWorkerThread(String name, WorkerTask task, boolean daemon);

}
