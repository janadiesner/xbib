
package org.xbib.io.jdbc.pool.bonecp.strategy;

import org.xbib.io.jdbc.pool.bonecp.ConnectionHandle;
import org.xbib.io.jdbc.pool.bonecp.Pool;
import org.xbib.io.jdbc.pool.bonecp.util.FinalizableReferenceQueue;
import org.xbib.io.jdbc.pool.bonecp.util.FinalizableWeakReference;

import java.lang.ref.Reference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A connection strategy that is optimized to store/retrieve the
 * connection inside a thread local variable. This makes getting a connection
 * in a managed thread environment such as Tomcat very fast but has the limitation
 * that it only works if # of threads <= # of connections.
 *
 * Should it detect that this isn't the case anymore, this class will flip back
 * permanently to the configured fallback strategy (i.e. default strategy)
 * and makes sure that currently assigned and unused connections are taken back.
 *
 */
public class CachedConnectionStrategy extends AbstractConnectionStrategy {

    /**
     * Keep track of connections tied to thread.
     */
    private final Map<ConnectionHandle, Reference<Thread>> threadFinalizableRefs = new ConcurrentHashMap<ConnectionHandle, Reference<Thread>>();

    private FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();
    /**
     * Obtain connections using this fallback strategy at first (or if this strategy cannot
     * succeed.
     */
    private ConnectionStrategy fallbackStrategy;

    /**
     * Connections are stored here.
     */
    private CachedConnectionStrategyThreadLocal<SimpleEntry<ConnectionHandle, Boolean>> tlConnections;

    public CachedConnectionStrategy(Pool pool, ConnectionStrategy fallbackStrategy) {
        this.pool = pool;
        this.fallbackStrategy = fallbackStrategy;
        this.tlConnections = new CachedConnectionStrategyThreadLocal<SimpleEntry<ConnectionHandle, Boolean>>(this.fallbackStrategy);
    }

    public CachedConnectionStrategyThreadLocal<SimpleEntry<ConnectionHandle, Boolean>> getTlConnections() {
        return tlConnections;
    }

    /**
     * Tries to close off all the unused assigned connections back to the pool. Assumes that
     * the strategy mode has already been flipped prior to calling this routine.
     * Called whenever our no of connection requests > no of threads.
     */
    protected synchronized void stealExistingAllocations() {
        for (ConnectionHandle handle : threadFinalizableRefs.keySet()) {
            // if they're not in use, pretend they are in use now and close them off.
            // this method assumes that the strategy has been flipped back to non-caching mode
            // prior to this method invocation.
            if (handle.isLogicallyClosed().compareAndSet(true, false)) {
                try {
                    pool.releaseConnection(handle);
                } catch (SQLException e) {
                }
            }
        }
        this.threadFinalizableRefs.clear();
    }

    protected void threadWatch(final ConnectionHandle c) {
        this.threadFinalizableRefs.put(c, new FinalizableWeakReference<Thread>(Thread.currentThread(), this.finalizableRefQueue) {
            public void finalizeReferent() {
                try {
                    c.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                CachedConnectionStrategy.this.threadFinalizableRefs.remove(c);
            }
        });
    }

    @Override
    protected Connection getConnectionInternal() throws SQLException {
        SimpleEntry<ConnectionHandle, Boolean> result = tlConnections.get();
        if (result == null) {
            pool.setCachedPoolStrategy(false);
            pool.setConnectionStrategy(fallbackStrategy);
            stealExistingAllocations();
            return pool.getConnection();
        }
        return result.getKey();
    }

    @Override
    public ConnectionHandle pollConnection() {
        throw new UnsupportedOperationException();
    }

    public void terminateAllConnections() {
        for (ConnectionHandle conn : threadFinalizableRefs.keySet()) {
            this.pool.destroyConnection(conn);
        }
        threadFinalizableRefs.clear();
        this.fallbackStrategy.terminateAllConnections();
    }

    @Override
    public void cleanupConnection(ConnectionHandle oldHandle, ConnectionHandle newHandle) {
    }

    public class CachedConnectionStrategyThreadLocal<T> extends ThreadLocal<SimpleEntry<ConnectionHandle, Boolean>> {

        private ConnectionStrategy fallbackStrategy;

        public CachedConnectionStrategyThreadLocal(ConnectionStrategy fallbackStrategy) {
            this.fallbackStrategy = fallbackStrategy;
        }

        @Override
        protected SimpleEntry<ConnectionHandle, Boolean> initialValue() {
            SimpleEntry<ConnectionHandle, Boolean> result = null;
            ConnectionHandle c = null;
            for (int i = 0; i < 4 * 3; i++) {
                c = (ConnectionHandle) this.fallbackStrategy.pollConnection();
                if (c != null) {
                    break;
                }
            }
            if (c != null) {
                result = new SimpleEntry<ConnectionHandle, Boolean>(c, false);
            }
            return result;
        }

        public SimpleEntry<ConnectionHandle, Boolean> dumbGet() {
            return super.get();
        }

        @Override
        public SimpleEntry<ConnectionHandle, Boolean> get() {
            SimpleEntry<ConnectionHandle, Boolean> result = super.get();
            if (result == null || result.getValue()) {
                ConnectionHandle fallbackConnection = (ConnectionHandle) this.fallbackStrategy.pollConnection();
                if (fallbackConnection == null) {
                    return null;
                }
                result = new SimpleEntry<ConnectionHandle, Boolean>(fallbackConnection, false);
            }
            result.setValue(true);
            result.getKey().isLogicallyClosed().set(false);
            return result;
        }
    }
}
