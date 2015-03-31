package org.snmp4j.util;

import java.util.EventListener;

/**
 * The <code>TreeListener</code> interface is implemented by objects
 * listening for tree events.
 */
public interface TreeListener extends EventListener {

    /**
     * Consumes the next table event, which is typically the next row in a
     * table retrieval operation.
     *
     * @param event a <code>TableEvent</code> instance.
     * @return <code>true</code> if this listener wants to receive more events,
     * otherwise return <code>false</code>. For example, a
     * <code>TreeListener</code> can return <code>false</code> to stop
     * tree retrieval.
     */
    boolean next(TreeEvent event);

    /**
     * Indicates in a series of tree events that no more events will follow.
     *
     * @param event a <code>TreeEvent</code> instance that will either indicate an error
     *              ({@link org.snmp4j.util.TreeEvent#isError()} returns <code>true</code>) or success
     *              of the tree retrieval operation.
     */
    void finished(TreeEvent event);

    /**
     * Indicates whether the tree walk is complete or not.
     *
     * @return <code>true</code> if it is complete, <code>false</code> otherwise.
     */
    boolean isFinished();
}
