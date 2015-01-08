/**
 *  Copyright 2010 Wallace Wadge
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.xbib.io.jdbc.pool.bonecp;

import org.xbib.io.jdbc.pool.bonecp.cache.StatementCache;
import org.xbib.io.jdbc.pool.bonecp.listener.ConnectionListener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper around JDBC Statement.
 *
 */
public class StatementHandle implements Statement {
    /**
     * Set to true if the connection has been "closed".
     */
    protected AtomicBoolean logicallyClosed = new AtomicBoolean();

    protected ConnectionListener connectionListener;

    /**
     * A handle to the actual statement.
     */
    protected Statement internalStatement;
    /**
     * SQL Statement used for this statement.
     */
    protected String sql;
    /**
     * Cache pertaining to this statement.
     */
    protected StatementCache cache;
    /**
     * Handle to the connection holding this statement.
     */
    protected ConnectionHandle connectionHandle;
    /**
     * If true, this statement is in the cache.
     */
    private volatile boolean inCache = false;

    private StringBuilder batchSQL = new StringBuilder();


    /**
     * Constructor to statement handle wrapper.
     *
     * @param internalStatement    handle to actual statement instance.
     * @param sql                  statement used for this handle.
     * @param cache                Cache handle
     * @param connectionHandle     Handle to the connection
     */
    public StatementHandle(Statement internalStatement, String sql, StatementCache cache,
                           ConnectionHandle connectionHandle, String cacheKey) {
        this.sql = sql;
        this.internalStatement = internalStatement;
        this.cache = cache;
        this.connectionHandle = connectionHandle;
        PoolConfig config = connectionHandle.getPool().getConfig();
        this.connectionListener = config.getConnectionListener();
        // store it in the cache if caching is enabled(unless it's already there).
        if (this.cache != null) {
            this.cache.putIfAbsent(cacheKey, this);
        }
    }


    /**
     * Constructor for empty statement (created via connection.createStatement)
     *
     * @param internalStatement    wrapper to statement
     * @param connectionHandle     Handle to the connection that this statement is tied
     */
    public StatementHandle(Statement internalStatement, ConnectionHandle connectionHandle) {
        this(internalStatement, null, null, connectionHandle, null);
    }


    public void close() throws SQLException {
        this.connectionHandle.untrackStatement(this);
        this.logicallyClosed.set(true);
        if (this.cache == null || !this.inCache) { // no cache = throw it away right now
            this.internalStatement.close();
        }
    }

    public void setInCache(boolean inCache) {
        this.inCache = inCache;
    }

    public void addBatch(String sql)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.addBatch(sql);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * Checks if the connection is marked as being logically open and throws an exception if not.
     *
     * @throws java.sql.SQLException if connection is marked as logically closed.
     */
    protected void checkClosed() throws SQLException {
        if (this.logicallyClosed.get()) {
            throw new SQLException("Statement is closed");
        }
    }

    public void cancel()
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.cancel();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void clearBatch()
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.clearBatch();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void clearWarnings()
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.clearWarnings();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    public boolean execute(String sql)
            throws SQLException {
        boolean result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.execute(sql);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }

        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }


    public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException {
        boolean result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.execute(sql, autoGeneratedKeys);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public boolean execute(String sql, int[] columnIndexes)
            throws SQLException {
        boolean result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.execute(sql, columnIndexes);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public boolean execute(String sql, String[] columnNames)
            throws SQLException {
        boolean result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.execute(sql, columnNames);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
        return result;

    }

    public int[] executeBatch()
            throws SQLException {
        int[] result;
        checkClosed();
        try {
            String query = "";
            if (this.connectionListener != null) {
                query = this.batchSQL.toString();
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, query);
            }
            result = this.internalStatement.executeBatch();
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, query);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }

    public ResultSet executeQuery(String sql)
            throws SQLException {
        ResultSet result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.executeQuery(sql);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }

    public int executeUpdate(String sql)
            throws SQLException {
        int result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.executeUpdate(sql);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public int executeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException {
        int result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.executeUpdate(sql, autoGeneratedKeys);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public int executeUpdate(String sql, int[] columnIndexes)
            throws SQLException {
        int result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.executeUpdate(sql, columnIndexes);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public int executeUpdate(String sql, String[] columnNames)
            throws SQLException {
        int result;
        checkClosed();
        try {
            if (this.connectionListener != null) {
                this.connectionListener.onBeforeStatementExecute(this.connectionHandle, this, sql);
            }
            result = this.internalStatement.executeUpdate(sql, columnNames);
            if (this.connectionListener != null) {
                this.connectionListener.onAfterStatementExecute(this.connectionHandle, this, sql);
            }
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }

        return result;
    }

    public Connection getConnection()
            throws SQLException {
        checkClosed();
        return this.connectionHandle;
    }

    public int getFetchDirection()
            throws SQLException {
        int result;
        checkClosed();
        try {
            result = this.internalStatement.getFetchDirection();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public int getFetchSize()
            throws SQLException {
        int result;
        checkClosed();
        try {
            result = this.internalStatement.getFetchSize();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }

    public ResultSet getGeneratedKeys()
            throws SQLException {
        ResultSet result;
        checkClosed();
        try {
            result = this.internalStatement.getGeneratedKeys();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }

    public int getMaxFieldSize()
            throws SQLException {
        int result;
        checkClosed();
        try {
            result = this.internalStatement.getMaxFieldSize();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }

    public int getMaxRows()
            throws SQLException {
        int result;
        checkClosed();
        try {
            result = this.internalStatement.getMaxRows();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }

    public boolean getMoreResults()
            throws SQLException {
        boolean result;
        checkClosed();
        try {
            result = this.internalStatement.getMoreResults();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public boolean getMoreResults(int current)
            throws SQLException {
        boolean result;
        checkClosed();

        try {
            result = this.internalStatement.getMoreResults(current);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
        return result;
    }

    public int getQueryTimeout()
            throws SQLException {
        int result;
        checkClosed();
        try {
            result = this.internalStatement.getQueryTimeout();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
        return result;
    }

    public ResultSet getResultSet()
            throws SQLException {
        ResultSet result;
        checkClosed();
        try {
            result = this.internalStatement.getResultSet();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }

    public int getResultSetConcurrency()
            throws SQLException {
        int result = 0;
        checkClosed();
        try {
            result = this.internalStatement.getResultSetConcurrency();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public int getResultSetHoldability()
            throws SQLException {
        int result = 0;
        checkClosed();
        try {
            result = this.internalStatement.getResultSetHoldability();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public int getResultSetType()
            throws SQLException {
        int result = 0;
        checkClosed();
        try {
            result = this.internalStatement.getResultSetType();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.Statement#getUpdateCount()
     */
    // @Override
    public int getUpdateCount()
            throws SQLException {
        int result = 0;
        checkClosed();
        try {
            result = this.internalStatement.getUpdateCount();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.Statement#getWarnings()
     */
    // @Override
    public SQLWarning getWarnings()
            throws SQLException {
        SQLWarning result = null;
        checkClosed();
        try {
            result = this.internalStatement.getWarnings();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    //@Override

    /**
     * Returns true if statement is logically closed
     *
     * @return True if handle is closed
     */
    public boolean isClosed() {
        return this.logicallyClosed.get();
    }

    // #ifdef JDK>6
    public void setPoolable(boolean poolable)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.setPoolable(poolable);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public boolean isWrapperFor(Class<?> iface)
            throws SQLException {
        boolean result = false;
        try {
            result = this.internalStatement.isWrapperFor(iface);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;
    }

    public <T> T unwrap(Class<T> iface)
            throws SQLException {
        T result = null;
        try {

            result = this.internalStatement.unwrap(iface);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }

    public boolean isPoolable()
            throws SQLException {
        boolean result = false;
        checkClosed();
        try {
            result = this.internalStatement.isPoolable();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
        return result;

    }
    // #endif JDK>6

    // #ifdef JDK7
    public void closeOnCompletion() throws SQLException {
        this.internalStatement.closeOnCompletion();
    }

    public boolean isCloseOnCompletion() throws SQLException {
        return this.internalStatement.isCloseOnCompletion();
    }
    // #endif JDK7


    public void setCursorName(String name)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.setCursorName(name);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.Statement#setEscapeProcessing(boolean)
     */
    // @Override
    public void setEscapeProcessing(boolean enable)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.setEscapeProcessing(enable);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.Statement#setFetchDirection(int)
     */
    // @Override
    public void setFetchDirection(int direction)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.setFetchDirection(direction);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.Statement#setFetchSize(int)
     */
    // @Override
    public void setFetchSize(int rows)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.setFetchSize(rows);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.Statement#setMaxFieldSize(int)
     */
    // @Override
    public void setMaxFieldSize(int max)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.setMaxFieldSize(max);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.Statement#setMaxRows(int)
     */
    // @Override
    public void setMaxRows(int max)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.setMaxRows(max);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.Statement#setQueryTimeout(int)
     */
    // @Override
    public void setQueryTimeout(int seconds)
            throws SQLException {
        checkClosed();
        try {
            this.internalStatement.setQueryTimeout(seconds);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }


    /**
     * Clears out the cache of statements.
     */
    protected void clearCache() {
        if (this.cache != null) {
            this.cache.clear();
        }
    }


    /**
     * Marks this statement as being "open"
     */
    public void setLogicallyOpen() {
        this.logicallyClosed.set(false);
    }

    public AtomicBoolean getLogicallyClosed() {
        return logicallyClosed;
    }


    @Override
    public String toString() {
        return this.sql;
    }

    /**
     * Returns the statement being wrapped around by this wrapper.
     *
     * @return the internalStatement being used.
     */
    public Statement getInternalStatement() {
        return this.internalStatement;
    }


    /**
     * Sets the internal statement used by this wrapper.
     *
     * @param internalStatement the internalStatement to set
     */
    public void setInternalStatement(Statement internalStatement) {
        this.internalStatement = internalStatement;
    }

}