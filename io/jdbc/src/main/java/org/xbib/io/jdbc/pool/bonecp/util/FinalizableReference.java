package org.xbib.io.jdbc.pool.bonecp.util;

/**
 * Implemented by references that have code to run after garbage collection of
 * their referents.
 *
 * @see FinalizableReferenceQueue
 * @author Bob Lee
 */
public interface FinalizableReference {

    /**
     * Invoked on a background thread after the referent has been garbage
     * collected unless security restrictions prevented starting a background
     * thread, in which case this method is invoked when new references
     * are created.
     */
    void finalizeReferent();
}