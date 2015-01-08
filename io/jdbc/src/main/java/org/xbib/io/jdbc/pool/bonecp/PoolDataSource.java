
package org.xbib.io.jdbc.pool.bonecp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.Closeable;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

/**
 * DataSource for use with lazy connection provider
 */
public class PoolDataSource extends PoolConfig implements DataSource, ObjectFactory, Closeable {
    /**
     * Config setting
     */
    private transient PrintWriter logWriter = null;
    /**
     * Pool handle
     */
    private transient FinalWrapper<Pool> pool = null;
    /**
     * JDBC driver
     */
    private String driverClass;
    /**
     * Constructs (and caches) a datasource on the fly based on the given username/password.
     */
    private LoadingCache<UsernamePassword, PoolDataSource> multiDataSource = CacheBuilder.newBuilder()
            .build(new CacheLoader<UsernamePassword, PoolDataSource>() {

                @Override
                public PoolDataSource load(UsernamePassword key) throws Exception {
                    PoolDataSource ds = new PoolDataSource(getConfig());
                    ds.setUsername(key.getUsername());
                    ds.setPassword(key.getPassword());
                    return ds;
                }
            });

    public PoolDataSource() {
    }

    /**
     * @param config
     */
    public PoolDataSource(PoolConfig config) {
        Field[] fields = PoolConfig.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                field.set(this, field.get(config));
            } catch (Exception e) {
                // should never happen
            }
        }
    }

    public Connection getConnection() throws SQLException {
        FinalWrapper<Pool> wrapper = this.pool;
        if (wrapper == null) {
            synchronized (this) {
                if (this.pool == null) {
                    try {
                        if (this.getDriverClass() != null) {
                            loadClass(this.getDriverClass());
                        }
                        this.pool = new FinalWrapper<Pool>(new Pool(this));
                    } catch (ClassNotFoundException e) {
                        throw new SQLException(e);
                    }
                }
                wrapper = this.pool;
            }
        }
        return wrapper.value.getConnection();
    }

    /**
     * Close the datasource (i.e. shut down the entire pool).
     */
    public void close() {
        if (getPool() != null) {
            getPool().shutdown();
        }
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        try {
            return this.multiDataSource.get(new UsernamePassword(username, password)).getConnection();
        } catch (ExecutionException e) {
            throw new SQLException("unable to obtain connection", e);
        }
    }

    /**
     * Retrieves the log writer for this DataSource object.
     */
    public PrintWriter getLogWriter() throws SQLException {
        return this.logWriter;
    }

    /**
     * Gets the maximum time in seconds that this data source can wait while
     * attempting to connect to a database.
     * A value of zero means that the timeout is the default system timeout if
     * there is one; otherwise, it means that there is no timeout.
     * When a DataSource object is created, the login timeout is initially zero.
     */
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("getLoginTimeout is unsupported");
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("getParentLogger is unsupported");
    }

    /**
     * Sets the log writer for this DataSource object to the given java.io.PrintWriter object.
     */
    public void setLogWriter(PrintWriter out)
            throws SQLException {
        this.logWriter = out;
    }

    /**
     * Sets the maximum time in seconds that this data source will wait while
     * attempting to connect to a database. A value of zero specifies that the timeout is the default
     * system timeout if there is one; otherwise, it specifies that there is no timeout.
     * When a DataSource object is created, the login timeout is initially zero.
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout is unsupported");
    }

    /**
     * Returns true if this either implements the interface argument or is directly or indirectly a wrapper for an object that does.
     *
     * @param arg0 class
     * @return true or false
     * @throws SQLException on error
     */
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return false;
    }

    /**
     * Returns an object that implements the given interface to allow access to non-standard methods,
     * or standard methods not exposed by the proxy.
     *
     * @param arg0 obj
     * @return unwrapped object
     * @throws SQLException
     */
    @SuppressWarnings("all")
    public Object unwrap(Class arg0) throws SQLException {
        return null;
    }

    /**
     * Gets driver class set in config.
     *
     * @return Driver class set in config
     */
    public String getDriverClass() {
        return this.driverClass;
    }


    /**
     * Sets driver to use (called via reflection).
     *
     * @param driverClass Driver to use
     */
    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }


    /**
     * Returns the total leased connections.
     *
     * @return total leased connections
     */
    public int getTotalLeased() {
        return getPool() == null ? 0 : getPool().getTotalLeased();
    }

    /**
     * Returns a configuration object built during initialization of the connection pool.
     *
     * @return the config
     */
    public PoolConfig getConfig() {
        return this;
    }

    public Object getObjectInstance(Object object, Name name, Context context, Hashtable<?, ?> table) throws Exception {
        Reference ref = (Reference) object;
        Enumeration<RefAddr> addrs = ref.getAll();
        Properties props = new Properties();
        while (addrs.hasMoreElements()) {
            RefAddr addr = addrs.nextElement();
            if (addr.getType().equals("driverClassName")) {
                Class.forName((String) addr.getContent());
            } else {
                props.put(addr.getType(), addr.getContent());
            }
        }
        PoolConfig config = new PoolConfig(props);
        return new PoolDataSource(config);
    }

    /**
     * Returns a handle to the pool. Useful to obtain a handle to the
     * statistics for example.
     *
     * @return pool
     */
    public Pool getPool() {
        return this.pool == null ? null : this.pool.value;
    }

    class FinalWrapper<T> {
        private final T value;

        public FinalWrapper(T value) {
            this.value = value;
        }
    }
}
