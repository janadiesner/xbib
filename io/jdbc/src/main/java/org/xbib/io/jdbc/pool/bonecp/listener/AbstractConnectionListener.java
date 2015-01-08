
package org.xbib.io.jdbc.pool.bonecp.listener;

import org.xbib.io.jdbc.pool.bonecp.ConnectionHandle;
import org.xbib.io.jdbc.pool.bonecp.StatementHandle;

import java.sql.SQLException;
import java.util.Map;

/**
 * A no-op implementation of the connection listener
 *
 */
public abstract class AbstractConnectionListener implements ConnectionListener {

    public void onAcquire(ConnectionHandle connection) {
        // do nothing
    }

    public void onCheckIn(ConnectionHandle connection) {
        // do nothing
    }

    public void onCheckOut(ConnectionHandle connection) {
        // do nothing
    }

    public void onDestroy(ConnectionHandle connection) {
        // do nothing
    }

    public boolean onAcquireFail(Throwable t, AcquireFailConfig acquireConfig) {
        boolean tryAgain = false;
        String log = acquireConfig.getLogMessage();
        try {
            Thread.sleep(acquireConfig.getAcquireRetryDelayInMs());
            if (acquireConfig.getAcquireRetryAttempts().get() > 0) {
                tryAgain = (acquireConfig.getAcquireRetryAttempts().decrementAndGet()) > 0;
            }
        } catch (Exception e) {
            tryAgain = false;
        }

        return tryAgain;
    }


    public boolean onConnectionException(ConnectionHandle connection, String state, Throwable t) {
        return true; // keep the default behaviour
    }

    public void onBeforeStatementExecute(ConnectionHandle conn, StatementHandle statement, String sql, Map<Object, Object> params) {
        // do nothing
    }

    public void onAfterStatementExecute(ConnectionHandle conn, StatementHandle statement, String sql, Map<Object, Object> params) {
        // do nothing
    }

    public ConnectionState onMarkPossiblyBroken(ConnectionHandle connection, String state, SQLException e) {
        return ConnectionState.NOP;
    }
}