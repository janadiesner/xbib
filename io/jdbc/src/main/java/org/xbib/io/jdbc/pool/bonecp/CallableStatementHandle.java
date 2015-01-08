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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;


/**
 * Wrapper around CallableStatement.
 */
public class CallableStatementHandle extends PreparedStatementHandle implements
        CallableStatement {
    /**
     * Handle to statement.
     */
    private CallableStatement internalCallableStatement;

    /**
     * CallableStatement constructor
     *
     * @param internalCallableStatement
     * @param sql
     * @param cache
     * @param connectionHandle
     * @param cacheKey key to cache
     */
    public CallableStatementHandle(CallableStatement internalCallableStatement,
                                   String sql, ConnectionHandle connectionHandle, String cacheKey, StatementCache cache) {
        super(internalCallableStatement, sql, connectionHandle, cacheKey, cache);
        this.internalCallableStatement = internalCallableStatement;
        this.connectionHandle = connectionHandle;
        this.sql = sql;
        this.cache = cache;
    }

    public Array getArray(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getArray(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }

    }

    public Array getArray(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getArray(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBigDecimal(int)
     */
    // @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBigDecimal(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBigDecimal(String)
     */
    // @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBigDecimal(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBigDecimal(int, int)
     */
    // @Override
    @Deprecated
    public BigDecimal getBigDecimal(int parameterIndex, int scale)
            throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBigDecimal(parameterIndex,
                    scale);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBlob(int)
     */
    // @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBlob(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBlob(String)
     */
    // @Override
    public Blob getBlob(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBlob(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBoolean(int)
     */
    // @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBoolean(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBoolean(String)
     */
    // @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBoolean(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getByte(int)
     */
    // @Override
    public byte getByte(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getByte(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getByte(String)
     */
    // @Override
    public byte getByte(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getByte(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBytes(int)
     */
    // @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBytes(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getBytes(String)
     */
    // @Override
    public byte[] getBytes(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getBytes(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    // #ifdef JDK>6
    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getCharacterStream(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getCharacterStream(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement
                    .getNCharacterStream(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement
                    .getNCharacterStream(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getNClob(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public NClob getNClob(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getNClob(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public String getNString(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getNString(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public String getNString(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getNString(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getRowId(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public RowId getRowId(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getRowId(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getSQLXML(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getSQLXML(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    public void setAsciiStream(String parameterName, InputStream x)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setAsciiStream(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setAsciiStream(String parameterName, InputStream x, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setAsciiStream(parameterName, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public void setBinaryStream(String parameterName, InputStream x)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBinaryStream(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setBinaryStream(String parameterName, InputStream x, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBinaryStream(parameterName, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBlob(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setBlob(String parameterName, InputStream inputStream)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBlob(parameterName, inputStream);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setBlob(String parameterName, InputStream inputStream,
                        long length) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBlob(parameterName, inputStream, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    public void setCharacterStream(String parameterName, Reader reader)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setCharacterStream(parameterName, reader);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setCharacterStream(String parameterName, Reader reader,
                                   long length) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setCharacterStream(parameterName, reader, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setClob(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setClob(String parameterName, Reader reader)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setClob(parameterName, reader);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setClob(String parameterName, Reader reader, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setClob(parameterName, reader, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setNCharacterStream(String parameterName, Reader value)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setNCharacterStream(parameterName, value);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setNCharacterStream(String parameterName, Reader value,
                                    long length) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setNCharacterStream(parameterName, value, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setNClob(parameterName, value);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setNClob(String parameterName, Reader reader)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setNClob(parameterName, reader);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setNClob(String parameterName, Reader reader, long length)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setNClob(parameterName, reader, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setNString(String parameterName, String value)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setNString(parameterName, value);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setRowId(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public void setSQLXML(String parameterName, SQLXML xmlObject)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setSQLXML(parameterName, xmlObject);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return this.internalCallableStatement.getObject(parameterIndex, type);
    }

    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return this.internalCallableStatement.getObject(parameterName, type);
    }

    public Clob getClob(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getClob(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public Clob getClob(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getClob(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    public Date getDate(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getDate(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }

    public Date getDate(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getDate(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getDate(int, java.util.Calendar)
     */
    // @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getDate(parameterIndex, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getDate(String,
     * java.util.Calendar)
     */
    // @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getDate(parameterName, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getDouble(int)
     */
    // @Override
    public double getDouble(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getDouble(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getDouble(String)
     */
    // @Override
    public double getDouble(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getDouble(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getFloat(int)
     */
    // @Override
    public float getFloat(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getFloat(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getFloat(String)
     */
    // @Override
    public float getFloat(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getFloat(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getInt(int)
     */
    // @Override
    public int getInt(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getInt(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getInt(String)
     */
    // @Override
    public int getInt(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getInt(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getLong(int)
     */
    // @Override
    public long getLong(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getLong(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getLong(String)
     */
    // @Override
    public long getLong(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getLong(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getObject(int)
     */
    // @Override
    public Object getObject(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getObject(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getObject(String)
     */
    // @Override
    public Object getObject(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getObject(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getObject(int, java.util.Map)
     */
    public Object getObject(int parameterIndex, Map<String, Class<?>> map)
            throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getObject(parameterIndex, map);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getObject(String,
     * java.util.Map)
     */
    // @Override
    public Object getObject(String parameterName, Map<String, Class<?>> map)
            throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getObject(parameterName, map);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getRef(int)
     */
    // @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getRef(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getRef(String)
     */
    // @Override
    public Ref getRef(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getRef(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getShort(int)
     */
    // @Override
    public short getShort(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getShort(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getShort(String)
     */
    // @Override
    public short getShort(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getShort(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getString(int)
     */
    // @Override
    public String getString(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getString(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getString(String)
     */
    // @Override
    public String getString(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getString(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getTime(int)
     */
    // @Override
    public Time getTime(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getTime(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getTime(String)
     */
    // @Override
    public Time getTime(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getTime(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getTime(int, java.util.Calendar)
     */
    // @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getTime(parameterIndex, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getTime(String,
     * java.util.Calendar)
     */
    // @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getTime(parameterName, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getTimestamp(int)
     */
    // @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getTimestamp(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getTimestamp(String)
     */
    // @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getTimestamp(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getTimestamp(int, java.util.Calendar)
     */
    // @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
            throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getTimestamp(parameterIndex,
                    cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getTimestamp(String,
     * java.util.Calendar)
     */
    // @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal)
            throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getTimestamp(parameterName,
                    cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getURL(int)
     */
    // @Override
    public URL getURL(int parameterIndex) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getURL(parameterIndex);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#getURL(String)
     */
    // @Override
    public URL getURL(String parameterName) throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.getURL(parameterName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#registerOutParameter(int, int)
     */
    // @Override
    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.registerOutParameter(parameterIndex, sqlType);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#registerOutParameter(String,
     * int)
     */
    // @Override
    public void registerOutParameter(String parameterName, int sqlType)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.registerOutParameter(parameterName, sqlType);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#registerOutParameter(int, int, int)
     */
    // @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.registerOutParameter(parameterIndex, sqlType, scale);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#registerOutParameter(int, int,
     * String)
     */
    // @Override
    public void registerOutParameter(int parameterIndex, int sqlType,
                                     String typeName) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.registerOutParameter(parameterIndex, sqlType, typeName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#registerOutParameter(String,
     * int, int)
     */
    // @Override
    public void registerOutParameter(String parameterName, int sqlType,
                                     int scale) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.registerOutParameter(parameterName, sqlType, scale);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#registerOutParameter(String,
     * int, String)
     */
    // @Override
    public void registerOutParameter(String parameterName, int sqlType,
                                     String typeName) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.registerOutParameter(parameterName, sqlType, typeName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setAsciiStream(String,
     * java.io.InputStream, int)
     */
    // @Override
    public void setAsciiStream(String parameterName, InputStream x, int length)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setAsciiStream(parameterName, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setBigDecimal(String,
     * java.math.BigDecimal)
     */
    // @Override
    public void setBigDecimal(String parameterName, BigDecimal x)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBigDecimal(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setBinaryStream(String,
     * java.io.InputStream, int)
     */
    // @Override
    public void setBinaryStream(String parameterName, InputStream x, int length)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBinaryStream(parameterName, x, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setBoolean(String, boolean)
     */
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBoolean(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setByte(String, byte)
     */
    // @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setByte(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setBytes(String, byte[])
     */
    // @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setBytes(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setCharacterStream(String,
     * java.io.Reader, int)
     */
    // @Override
    public void setCharacterStream(String parameterName, Reader reader,
                                   int length) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setCharacterStream(parameterName, reader, length);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }

    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setDate(String, java.sql.Date)
     */
    // @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setDate(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setDate(String, java.sql.Date,
     * java.util.Calendar)
     */
    // @Override
    public void setDate(String parameterName, Date x, Calendar cal)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setDate(parameterName, x, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setDouble(String, double)
     */
    // @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setDouble(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setFloat(String, float)
     */
    // @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setFloat(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setInt(String, int)
     */
    // @Override
    public void setInt(String parameterName, int x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setInt(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setLong(String, long)
     */
    // @Override
    public void setLong(String parameterName, long x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setLong(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setNull(String, int)
     */
    public void setNull(String parameterName, int sqlType) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setNull(parameterName, sqlType);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setNull(String, int,
     * String)
     */
    // @Override
    public void setNull(String parameterName, int sqlType, String typeName)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setNull(parameterName, sqlType, typeName);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setObject(String,
     * Object)
     */
    // @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setObject(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setObject(String,
     * Object, int)
     */
    // @Override
    public void setObject(String parameterName, Object x, int targetSqlType)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setObject(parameterName, x, targetSqlType);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setObject(String,
     * Object, int, int)
     */
    // @Override
    public void setObject(String parameterName, Object x, int targetSqlType,
                          int scale) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setObject(parameterName, x, targetSqlType, scale);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }


    public void setShort(String parameterName, short x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setShort(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setString(String,
     * String)
     */
    // @Override
    public void setString(String parameterName, String x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setString(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setTime(String parameterName, Time x) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setTime(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setTime(String parameterName, Time x, Calendar cal)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setTime(parameterName, x, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setTimestamp(String parameterName, Timestamp x)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setTimestamp(parameterName, x);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
            throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setTimestamp(parameterName, x, cal);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#setURL(String, java.net.URL)
     */
    // @Override
    public void setURL(String parameterName, URL val) throws SQLException {
        checkClosed();
        try {
            this.internalCallableStatement.setURL(parameterName, val);
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.sql.CallableStatement#wasNull()
     */
    // @Override
    public boolean wasNull() throws SQLException {
        checkClosed();
        try {
            return this.internalCallableStatement.wasNull();
        } catch (SQLException e) {
            throw this.connectionHandle.markPossiblyBroken(e);

        }
    }

    /**
     * Returns the callable statement that this wrapper wraps.
     *
     * @return the internalCallableStatement currently being used.
     */
    public CallableStatement getInternalCallableStatement() {
        return this.internalCallableStatement;
    }

    /**
     * Sets the callable statement used by this wrapper.
     *
     * @param internalCallableStatement the internalCallableStatement to set
     */
    public void setInternalCallableStatement(
            CallableStatement internalCallableStatement) {
        this.internalCallableStatement = internalCallableStatement;
    }
}