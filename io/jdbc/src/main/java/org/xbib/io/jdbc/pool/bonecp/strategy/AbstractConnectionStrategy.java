
package org.xbib.io.jdbc.pool.bonecp.strategy;

import org.xbib.io.jdbc.pool.bonecp.ConnectionHandle;
import org.xbib.io.jdbc.pool.bonecp.Pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class for the different pool strategies
 *
 */
public abstract class AbstractConnectionStrategy implements ConnectionStrategy {

    /**
     * Pool handle
     */
    protected Pool pool;

    /**
     * Prevent repeated termination of all connections when the DB goes down.
     */
    protected Lock terminationLock = new ReentrantLock();


    /**
     * Prep for a new connection
     *
     * @return if stats are enabled, return the nanoTime when this connection was requested.
     * @throws java.sql.SQLException
     */
    protected long preConnection() throws SQLException {
        long statsObtainTime = 0;
        if (pool.getConfig().isStatisticsEnabled()) {
            statsObtainTime = System.nanoTime();
            this.pool.getStatistics().incrementConnectionsRequested();
        }
        return statsObtainTime;
    }


    /**
     * After obtaining a connection, perform additional tasks.
     *
     * @param handle
     * @param statsObtainTime
     */
    protected void postConnection(ConnectionHandle handle, long statsObtainTime) {

        handle.renewConnection(); // mark it as being logically "open"

        // Give an application a chance to do something with it.
        if (handle.getConnectionListener() != null) {
            handle.getConnectionListener().onCheckOut(handle);
        }

        if (this.pool.getConfig().isStatisticsEnabled()) {
            this.pool.getStatistics().addCumulativeConnectionWaitTime(System.nanoTime() - statsObtainTime);
        }
    }

    public Connection getConnection() throws SQLException {
        long statsObtainTime = preConnection();

        ConnectionHandle result = (ConnectionHandle) getConnectionInternal();
        if (result != null) {
            postConnection(result, statsObtainTime);
        }

        return result;
    }

    /**
     * Actual call that returns a connection
     *
     * @return Connection
     * @throws java.sql.SQLException
     */
    protected abstract Connection getConnectionInternal() throws SQLException;


    public ConnectionHandle pollConnection() {
        // usually overridden
        return null;
    }

    public void cleanupConnection(ConnectionHandle oldHandle,
                                  ConnectionHandle newHandle) {
        // do nothing
    }

}
