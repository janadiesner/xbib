
package org.xbib.io.jdbc.pool.bonecp;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.jdbc.pool.bonecp.listener.AcquireFailConfig;
import org.xbib.io.jdbc.pool.bonecp.listener.ConnectionListener;
import org.xbib.io.jdbc.pool.bonecp.strategy.CachedConnectionStrategy;
import org.xbib.io.jdbc.pool.bonecp.strategy.ConnectionStrategy;
import org.xbib.io.jdbc.pool.bonecp.strategy.DefaultConnectionStrategy;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection pool
 */
public class Pool implements Closeable {

    private static final Logger logger = LogManager.getLogger(Pool.class.getSimpleName());

    /**
     * Create more connections when we hit x% of our possible number of connections.
     */
    protected final int poolAvailabilityThreshold;
    /**
     * Number of partitions passed in constructor
     */
    protected int partitionCount;
    /**
     * Partitions handle.
     */
    protected ConnectionPartition[] partitions;
    /**
     * Handle to factory that creates 1 thread per partition that periodically wakes up and performs some
     * activity on the connection.
     */
    protected ScheduledExecutorService keepAliveScheduler;
    /**
     * Handle to factory that creates 1 thread per partition that periodically wakes up and performs some
     * activity on the connection.
     */
    private ScheduledExecutorService maxAliveScheduler;
    /**
     * Executor for threads watching each partition to dynamically create new threads/kill off excess ones.
     */
    private ExecutorService connectionsScheduler;
    /**
     * Configuration object used in constructor.
     */
    private PoolConfig config;
    /**
     * Executor service for obtaining a connection in an asynchronous fashion.
     */
    private ListeningExecutorService asyncExecutor;

    /**
     * set to true if the connection pool has been flagged as shutting down.
     */
    protected volatile boolean poolShuttingDown;

    /**
     * Time to wait before timing out the connection. Default in config is Long.MAX_VALUE milliseconds.
     */
    protected long connectionTimeoutInMs;
    /**
     * if true, we care about statistics.
     */
    protected boolean statisticsEnabled;
    /**
     * statistics handle.
     */
    protected Statistics statistics = new Statistics(this);

    protected boolean resetConnectionOnClose;

    protected boolean cachedPoolStrategy;
    /**
     * Currently active get connection strategy class to use.
     */
    private ConnectionStrategy connectionStrategy;
    /**
     * If true, there are no connections to be taken.
     */
    private AtomicBoolean dbIsDown = new AtomicBoolean();
    /**
     * Config setting.
     */
    protected Properties clientInfo;
    /**
     * If false, we haven't made a dummy driver call first.
     */
    protected volatile boolean driverInitialized = false;
    /**
     * Keep track of our jvm version.
     */
    //protected int jvmMajorVersion;
    /**
     * This is moved here to aid testing.
     */
    protected static String connectionClass = "java.sql.Connection";

    /**
     * Constructor
     *
     * @param config Configuration for pool
     * @throws SQLException on error
     */
    public Pool(PoolConfig config) throws SQLException {

        try {
            this.config = Preconditions.checkNotNull(config).clone();
        } catch (CloneNotSupportedException e1) {
            throw new SQLException("Cloning of the config failed");
        }
        this.config.sanitize();
        this.statisticsEnabled = this.config.isStatisticsEnabled();
        this.poolAvailabilityThreshold = this.config.getPoolAvailabilityThreshold();
        this.connectionTimeoutInMs = this.config.getConnectionTimeoutInMs();
        this.resetConnectionOnClose = this.config.isResetConnectionOnClose();
        this.clientInfo = this.config.getClientInfo();

        AcquireFailConfig acquireConfig = new AcquireFailConfig();
        acquireConfig.setAcquireRetryAttempts(new AtomicInteger(0));
        acquireConfig.setAcquireRetryDelayInMs(0);
        acquireConfig.setLogMessage("Failed to obtain initial connection");

        if (!this.config.isLazyInit()) {
            try {
                Connection sanityConnection = obtainRawInternalConnection();
                sanityConnection.close();
            } catch (Exception e) {
                if (this.config.getConnectionListener() != null) {
                    this.config.getConnectionListener().onAcquireFail(e, acquireConfig);
                }
                throw new SQLException(e.getMessage(), e);
            }
        }
        this.asyncExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        this.partitions = new ConnectionPartition[this.config.getPartitionCount()];
        String suffix = "";
        if (this.config.getPoolName() != null) {
            suffix = "-" + this.config.getPoolName();
        }
        this.keepAliveScheduler = Executors.newScheduledThreadPool(this.config.getPartitionCount(), new CustomThreadFactory("jdbc-pool-keep-alive-scheduler" + suffix, true));
        this.maxAliveScheduler = Executors.newScheduledThreadPool(this.config.getPartitionCount(), new CustomThreadFactory("jdbc-pool-max-alive-scheduler" + suffix, true));
        this.connectionsScheduler = Executors.newFixedThreadPool(this.config.getPartitionCount(), new CustomThreadFactory("jdbc-pool-watch-thread" + suffix, true));

        this.partitionCount = this.config.getPartitionCount();
        this.cachedPoolStrategy = this.config.getPoolStrategy() != null && this.config.getPoolStrategy().equalsIgnoreCase("CACHED");
        if (this.cachedPoolStrategy) {
            this.connectionStrategy = new CachedConnectionStrategy(this, new DefaultConnectionStrategy(this));
        } else {
            this.connectionStrategy = new DefaultConnectionStrategy(this);
        }
        boolean queueLIFO = this.config.getServiceOrder() != null && this.config.getServiceOrder().equalsIgnoreCase("LIFO");
        for (int p = 0; p < this.config.getPartitionCount(); p++) {
            ConnectionPartition connectionPartition = new ConnectionPartition(this);
            this.partitions[p] = connectionPartition;
            BlockingQueue<ConnectionHandle> connectionHandles = new LinkedBlockingQueue<ConnectionHandle>(this.config.getMaxConnectionsPerPartition());
            this.partitions[p].setFreeConnections(connectionHandles);
            if (!this.config.isLazyInit()) {
                for (int i = 0; i < this.config.getMinConnectionsPerPartition(); i++) {
                    this.partitions[p].addFreeConnection(new ConnectionHandle(null, this.partitions[p], this, false));
                }
            }
            if (this.config.getIdleConnectionTestPeriod(TimeUnit.SECONDS) > 0 || this.config.getIdleMaxAge(TimeUnit.SECONDS) > 0) {
                final Runnable connectionPingThread = new ConnectionPingThread(connectionPartition, this.keepAliveScheduler, this, this.config.getIdleMaxAge(TimeUnit.MILLISECONDS), this.config.getIdleConnectionTestPeriod(TimeUnit.MILLISECONDS), queueLIFO);
                long delayInSeconds = this.config.getIdleConnectionTestPeriod(TimeUnit.SECONDS);
                if (delayInSeconds == 0L) {
                    delayInSeconds = this.config.getIdleMaxAge(TimeUnit.SECONDS);
                }
                if (this.config.getIdleMaxAge(TimeUnit.SECONDS) < delayInSeconds
                        && this.config.getIdleConnectionTestPeriod(TimeUnit.SECONDS) != 0
                        && this.config.getIdleMaxAge(TimeUnit.SECONDS) != 0) {
                    delayInSeconds = this.config.getIdleMaxAge(TimeUnit.SECONDS);
                }
                this.keepAliveScheduler.schedule(connectionPingThread, delayInSeconds, TimeUnit.SECONDS);
            }
            if (this.config.getMaxConnectionAgeInSeconds() > 0) {
                final Runnable connectionMaxAgeThread = new ConnectionMaxAgeThread(connectionPartition, this.maxAliveScheduler, this, this.config.getMaxConnectionAge(TimeUnit.MILLISECONDS), queueLIFO);
                this.maxAliveScheduler.schedule(connectionMaxAgeThread, this.config.getMaxConnectionAgeInSeconds(), TimeUnit.SECONDS);
            }
            // watch this partition for low no of threads
            this.connectionsScheduler.execute(new PoolWatchThread(connectionPartition, this));
        }
    }

    /**
     * Closes off this connection pool.
     */
    public synchronized void shutdown() {
        if (!this.poolShuttingDown) {
            this.poolShuttingDown = true;
            logger.info("shutting down connection pool");
            this.keepAliveScheduler.shutdownNow(); // stop threads from firing.
            this.maxAliveScheduler.shutdownNow(); // stop threads from firing.
            this.connectionsScheduler.shutdownNow(); // stop threads from firing.
            this.asyncExecutor.shutdownNow();
            try {
                this.connectionsScheduler.awaitTermination(5, TimeUnit.SECONDS);
                this.maxAliveScheduler.awaitTermination(5, TimeUnit.SECONDS);
                this.keepAliveScheduler.awaitTermination(5, TimeUnit.SECONDS);
                this.asyncExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // do nothing
            }
            this.connectionStrategy.terminateAllConnections();
            unregisterDriver();
            logger.info("connection pool has been shutdown");
        }
    }

    /**
     * Drops a driver from the DriverManager's list.
     */
    protected void unregisterDriver() {
        String jdbcURL = this.config.getJdbcUrl();
        if ((jdbcURL != null) && this.config.isDeregisterDriverOnClose()) {
            logger.info("Unregistering JDBC driver for : " + jdbcURL);
            try {
                DriverManager.deregisterDriver(DriverManager.getDriver(jdbcURL));
            } catch (SQLException e) {
                logger.info("Unregistering driver failed.", e);
            }
        }
    }

    /**
     * Just a synonym to shutdown.
     */
    public void close() {
        shutdown();
    }


    /**
     * Physically close off the internal connection.
     *
     * @param conn
     */
    public void destroyConnection(ConnectionHandle conn) {
        postDestroyConnection(conn);
        conn.setInReplayMode(true); // we're dead, stop attempting to replay anything
        try {
            conn.internalClose();
        } catch (SQLException e) {
            logger.error("error in attempting to close connection", e);
        }
    }

    /**
     * Update counters and call hooks.
     *
     * @param handle connection handle.
     */
    protected void postDestroyConnection(ConnectionHandle handle) {
        ConnectionPartition partition = handle.getOriginatingPartition();

        partition.updateCreatedConnections(-1);
        partition.setUnableToCreateMoreTransactions(false); // we can create new ones now, this is an optimization


        // "Destroying" for us means: don't put it back in the pool.
        if (handle.getConnectionListener() != null) {
            handle.getConnectionListener().onDestroy(handle);
        }

    }

    /**
     * Obtains a database connection, retrying if necessary.
     *
     * @param connectionHandle
     * @return A DB connection.
     * @throws SQLException
     */
    public Connection obtainInternalConnection(ConnectionHandle connectionHandle) throws SQLException {
        boolean tryAgain = false;
        Connection result = null;
        Connection oldRawConnection = connectionHandle.getInternalConnection();
        String url = this.getConfig().getJdbcUrl();

        int acquireRetryAttempts = this.getConfig().getAcquireRetryAttempts();
        long acquireRetryDelayInMs = this.getConfig().getAcquireRetryDelayInMs();
        AcquireFailConfig acquireConfig = new AcquireFailConfig();
        acquireConfig.setAcquireRetryAttempts(new AtomicInteger(acquireRetryAttempts));
        acquireConfig.setAcquireRetryDelayInMs(acquireRetryDelayInMs);
        acquireConfig.setLogMessage("Failed to acquire connection to " + url);
        ConnectionListener connectionListener = this.getConfig().getConnectionListener();
        do {
            result = null;
            try {
                // keep track of this hook.
                result = this.obtainRawInternalConnection();
                tryAgain = false;

                if (acquireRetryAttempts != this.getConfig().getAcquireRetryAttempts()) {
                    logger.info("Successfully re-established connection to " + url);
                }

                this.getDbIsDown().set(false);

                connectionHandle.setInternalConnection(result);

                // call the hook, if available.
                if (connectionListener != null) {
                    connectionListener.onAcquire(connectionHandle);
                }


                ConnectionHandle.sendInitSQL(result, this.getConfig().getInitSQL());
            } catch (SQLException e) {
                // call the hook, if available.
                if (connectionListener != null) {
                    tryAgain = connectionListener.onAcquireFail(e, acquireConfig);
                } else {
                    logger.error(String.format("Failed to acquire connection to %s. Sleeping for %d ms. Attempts left: %d", url, acquireRetryDelayInMs, acquireRetryAttempts), e);

                    try {
                        if (acquireRetryAttempts > 0) {
                            Thread.sleep(acquireRetryDelayInMs);
                        }
                        tryAgain = (acquireRetryAttempts--) > 0;
                    } catch (InterruptedException e1) {
                        tryAgain = false;
                    }
                }
                if (!tryAgain) {
                    if (oldRawConnection != null) {
                        oldRawConnection.close();
                    }
                    if (result != null) {
                        result.close();
                    }
                    connectionHandle.setInternalConnection(oldRawConnection);
                    throw e;
                }
            }
        } while (tryAgain);

        return result;

    }

    /**
     * Returns a database connection by using Driver.getConnection() or DataSource.getConnection()
     *
     * @return Connection handle
     * @throws java.sql.SQLException on error
     */
    @SuppressWarnings("resource")
    protected Connection obtainRawInternalConnection()
            throws SQLException {
        Connection result = null;

        DataSource datasourceBean = this.config.getDatasourceBean();
        String url = this.config.getJdbcUrl();
        String username = this.config.getUsername();
        String password = this.config.getPassword();
        Properties props = this.config.getDriverProperties();
        boolean externalAuth = this.config.isExternalAuth();
        if (externalAuth &&
                props == null) {
            props = new Properties();
        }

        if (datasourceBean != null) {
            return (username == null ? datasourceBean.getConnection() : datasourceBean.getConnection(username, password));
        }

        // just force the driver to init first
        if (!this.driverInitialized) {
            try {
                this.driverInitialized = true;
                if (props != null) {
                    result = DriverManager.getConnection(url, props);
                } else {
                    result = DriverManager.getConnection(url, username, password);
                }
                result.close();
            } catch (SQLException t) {
                // just force the driver to init first
            }
        }

        if (props != null) {
            result = DriverManager.getConnection(url, props);
        } else {
            result = DriverManager.getConnection(url, username, password);
        }
        if (this.clientInfo != null) { // we take care of null'ing this in the constructor if jdk < 6
            result.setClientInfo(this.clientInfo);
        }
        return result;
    }


    public ConnectionPartition[] getPartitions() {
        return partitions;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public void setConnectionStrategy(ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
    }

    public ConnectionStrategy getConnectionStrategy() {
        return connectionStrategy;
    }

    public void setCachedPoolStrategy(boolean enable) {
        this.cachedPoolStrategy = enable;
    }

    public long getConnectionTimeoutInMs() {
        return connectionTimeoutInMs;
    }

    /**
     * Returns a free connection.
     *
     * @return Connection handle.
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return connectionStrategy.getConnection();
    }

    /**
     * Throw an exception to capture it so as to be able to print it out later on
     *
     * @param message message to display
     * @return Stack trace message
     */
    protected String captureStackTrace(String message) {
        StringBuilder stringBuilder = new StringBuilder(String.format(message, Thread.currentThread().getName()));
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            stringBuilder.append(" " + trace[i] + "\r\n");
        }
        stringBuilder.append("");
        return stringBuilder.toString();
    }

    /**
     * Obtain a connection asynchronously by queueing a request to obtain a connection in a separate thread.
     * <p/>
     * Use as follows:<p>
     * Future&lt;Connection&gt; result = pool.getAsyncConnection();<p>
     * ... do something else in your application here ...<p>
     * Connection connection = result.get(); // get the connection<p>
     *
     * @return A Future task returning a connection.
     */
    public ListenableFuture<Connection> getAsyncConnection() {

        return asyncExecutor.submit(new Callable<Connection>() {
            public Connection call() throws Exception {
                return getConnection();
            }
        });
    }

    /**
     * Tests if this partition has hit a threshold and signal to the pool watch thread to create new connections
     *
     * @param connectionPartition to test for.
     */
    public void maybeSignalForMoreConnections(ConnectionPartition connectionPartition) {

        if (!connectionPartition.isUnableToCreateMoreTransactions()
                && !this.poolShuttingDown &&
                connectionPartition.getAvailableConnections() * 100 / connectionPartition.getMaxConnections() <= this.poolAvailabilityThreshold) {
            connectionPartition.getPoolWatchThreadSignalQueue().offer(new Object()); // item being pushed is not important.
        }
    }

    /**
     * Releases the given connection back to the pool. This method is not intended to be called by
     * applications (hence set to protected). Call connection.close() instead which will return
     * the connection back to the pool.
     *
     * @param connection to release
     * @throws java.sql.SQLException
     */
    public void releaseConnection(Connection connection) throws SQLException {
        ConnectionHandle handle = (ConnectionHandle) connection;
        if (handle.getConnectionListener() != null) {
            handle.getConnectionListener().onCheckIn(handle);
        }
        // release immediately or place it in a queue so that another thread will eventually close it. If we're shutting down,
        // close off the connection right away because the helper threads have gone away.
        if (!this.poolShuttingDown) {
            internalReleaseConnection(handle);
        }
    }

    /**
     * Release a connection by placing the connection back in the pool.
     *
     * @param connectionHandle Connection being released.
     * @throws java.sql.SQLException
     */
    private void internalReleaseConnection(ConnectionHandle connectionHandle) throws SQLException {
        if (!this.cachedPoolStrategy) {
            connectionHandle.clearStatementCaches(false);
        }

        if (connectionHandle.getReplayLog() != null) {
            connectionHandle.getReplayLog().clear();
            connectionHandle.getRecoveryResult().getReplaceTarget().clear();
        }

        if (connectionHandle.isExpired() ||
                (!this.poolShuttingDown
                        && connectionHandle.isPossiblyBroken()
                        && !isConnectionHandleAlive(connectionHandle))) {

            if (connectionHandle.isExpired()) {
                connectionHandle.internalClose();
            }

            ConnectionPartition connectionPartition = connectionHandle.getOriginatingPartition();
            postDestroyConnection(connectionHandle);

            maybeSignalForMoreConnections(connectionPartition);
            connectionHandle.clearStatementCaches(true);
            return; // don't place back in queue - connection is broken or expired.
        }


        connectionHandle.setConnectionLastUsedInMs(System.currentTimeMillis());
        if (!this.poolShuttingDown) {
            putConnectionBackInPartition(connectionHandle);
        } else {
            connectionHandle.internalClose();
        }
    }


    /**
     * Places a connection back in the originating partition.
     *
     * @param connectionHandle to place back
     * @throws SQLException on error
     */
    protected void putConnectionBackInPartition(ConnectionHandle connectionHandle) throws SQLException {

        if (this.cachedPoolStrategy && ((CachedConnectionStrategy) this.connectionStrategy).getTlConnections().dumbGet().getValue()) {
            connectionHandle.isLogicallyClosed().set(true);
            ((CachedConnectionStrategy) this.connectionStrategy).getTlConnections().set(new AbstractMap.SimpleEntry<ConnectionHandle, Boolean>(connectionHandle, false));
        } else {
            BlockingQueue<ConnectionHandle> queue = connectionHandle.getOriginatingPartition().getFreeConnections();
            if (!queue.offer(connectionHandle)) { // this shouldn't fail
                connectionHandle.internalClose();
            }
        }
    }


    /**
     * Sends a dummy statement to the server to keep the connection alive
     *
     * @param connection Connection handle to perform activity on
     * @return true if test query worked, false otherwise
     */
    public boolean isConnectionHandleAlive(ConnectionHandle connection) {
        Statement stmt = null;
        boolean result = false;
        boolean logicallyClosed = connection.isLogicallyClosed().get();
        try {
            connection.isLogicallyClosed().compareAndSet(true, false); // avoid checks later on if it's marked as closed.
            String testStatement = this.config.getConnectionTestStatement();
            ResultSet rs = null;
            if (testStatement == null) {
                // Make a call to fetch the metadata instead of a dummy query.
                rs = connection.getMetaData().getTables(null, null, "KEEPALIVE", new String[]{"TABLE"});
            } else {
                stmt = connection.createStatement();
                stmt.execute(testStatement);
            }
            if (rs != null) {
                rs.close();
            }
            result = true;
        } catch (SQLException e) {
            // connection must be broken
            result = false;
        } finally {
            connection.isLogicallyClosed().set(logicallyClosed);
            connection.setConnectionLastResetInMs(System.currentTimeMillis());
            result = closeStatement(stmt, result);
        }
        return result;
    }

    /**
     * @param stmt statement
     * @param result the result
     * @return false on failure.
     */
    private boolean closeStatement(Statement stmt, boolean result) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                return false;
            }
        }
        return result;
    }

    /**
     * Return total number of connections currently in use by an application
     *
     * @return no of leased connections
     */
    public int getTotalLeased() {
        int total = 0;
        for (int i = 0; i < this.partitionCount && this.partitions[i] != null; i++) {
            total += this.partitions[i].getCreatedConnections() - this.partitions[i].getAvailableConnections();
        }
        return total;
    }

    /**
     * Return the number of free connections available to an application right away (excluding connections that can be
     * created dynamically)
     *
     * @return number of free connections
     */
    public int getTotalFree() {
        int total = 0;
        for (int i = 0; i < this.partitionCount && this.partitions[i] != null; i++) {
            total += this.partitions[i].getAvailableConnections();
        }
        return total;
    }

    /**
     * Return total number of connections created in all partitions.
     *
     * @return number of created connections
     */
    public int getTotalCreatedConnections() {
        int total = 0;
        for (int i = 0; i < this.partitionCount && this.partitions[i] != null; i++) {
            total += this.partitions[i].getCreatedConnections();
        }
        return total;
    }


    /**
     * Gets config object.
     *
     * @return config object
     */
    public PoolConfig getConfig() {
        return this.config;
    }

    /**
     * Returns a reference to the statistics class.
     *
     * @return statistics
     */
    public Statistics getStatistics() {
        return this.statistics;
    }

    /**
     * Returns the dbIsDown field.
     *
     * @return dbIsDown
     */
    public AtomicBoolean getDbIsDown() {
        return this.dbIsDown;
    }

    class PoolWatchThread implements Runnable {
        /**
         * Partition being monitored.
         */
        private ConnectionPartition partition;
        /**
         * Pool handle.
         */
        private Pool pool;
        /**
         * How long to wait before retrying to add a connection upon failure.
         */
        private long acquireRetryDelayInMs = 1000L;
        /**
         * Start off lazily.
         */
        protected boolean lazyInit;
        /**
         * Occupancy% threshold.
         */
        private int poolAvailabilityThreshold;


        /**
         * Thread constructor
         *
         * @param connectionPartition partition to monitor
         * @param pool                Pool handle.
         */
        public PoolWatchThread(ConnectionPartition connectionPartition, Pool pool) {
            this.partition = connectionPartition;
            this.pool = pool;
            this.lazyInit = this.pool.getConfig().isLazyInit();
            this.acquireRetryDelayInMs = this.pool.getConfig().getAcquireRetryDelayInMs();
            this.poolAvailabilityThreshold = this.pool.getConfig().getPoolAvailabilityThreshold();
        }


        public void run() {
            int maxNewConnections;
            while (true) {
                try {
                    if (this.lazyInit) { // block the first time if this is on.
                        this.partition.getPoolWatchThreadSignalQueue().take();
                    }
                    maxNewConnections = this.partition.getMaxConnections() - this.partition.getCreatedConnections();
                    // loop for spurious interrupt
                    while (maxNewConnections == 0 || (this.partition.getAvailableConnections() * 100 / this.partition.getMaxConnections() > this.poolAvailabilityThreshold)) {
                        if (maxNewConnections == 0) {
                            this.partition.setUnableToCreateMoreTransactions(true);
                        }

                        this.partition.getPoolWatchThreadSignalQueue().take();
                        maxNewConnections = this.partition.getMaxConnections() - this.partition.getCreatedConnections();

                    }
                    if (maxNewConnections > 0
                            && !this.pool.poolShuttingDown) {
                        fillConnections(Math.min(maxNewConnections, this.partition.getAcquireIncrement()));
                        // for the case where we have killed off all our connections due to network/db error
                        if (this.partition.getCreatedConnections() < this.partition.getMinConnections()) {
                            fillConnections(this.partition.getMinConnections() - this.partition.getCreatedConnections());

                        }
                    }
                    if (this.pool.poolShuttingDown) {
                        return;
                    }
                } catch (InterruptedException e) {
                    return; // we've been asked to terminate
                }
            }
        }


        /**
         * Adds new connections to the partition.
         *
         * @param connectionsToCreate number of connections to create
         * @throws InterruptedException
         */
        private void fillConnections(int connectionsToCreate) throws InterruptedException {
            try {
                for (int i = 0; i < connectionsToCreate; i++) {
                    if (this.pool.poolShuttingDown) {
                        break;
                    }
                    this.partition.addFreeConnection(new ConnectionHandle(null, this.partition, this.pool, false));
                }
            } catch (Exception e) {
                Thread.sleep(this.acquireRetryDelayInMs);
            }

        }
    }

    class ConnectionMaxAgeThread implements Runnable {

        /**
         * Max no of ms to wait before a connection that isn't used is killed off.
         */
        private long maxAgeInMs;
        /**
         * Partition being handled.
         */
        private ConnectionPartition partition;
        /**
         * Scheduler handle. *
         */
        private ScheduledExecutorService scheduler;
        /**
         * Handle to connection pool.
         */
        private Pool pool;
        /**
         * If true, we're operating in a LIFO fashion.
         */
        private boolean lifoMode;

        /**
         * Constructor
         *
         * @param connectionPartition partition to work on
         * @param scheduler           Scheduler handler.
         * @param pool                pool handle
         * @param maxAgeInMs          Threads older than this are killed off
         * @param lifoMode            if true, we're running under a lifo fashion.
         */
        protected ConnectionMaxAgeThread(ConnectionPartition connectionPartition, ScheduledExecutorService scheduler,
                                         Pool pool, long maxAgeInMs, boolean lifoMode) {
            this.partition = connectionPartition;
            this.scheduler = scheduler;
            this.maxAgeInMs = maxAgeInMs;
            this.pool = pool;
            this.lifoMode = lifoMode;
        }


        /**
         * Invoked periodically.
         */
        public void run() {
            ConnectionHandle connection;
            long tmp;
            long nextCheckInMs = this.maxAgeInMs;

            int partitionSize = this.partition.getAvailableConnections();
            long currentTime = System.currentTimeMillis();
            for (int i = 0; i < partitionSize; i++) {
                try {
                    connection = this.partition.getFreeConnections().poll();

                    if (connection != null) {
                        connection.setOriginatingPartition(this.partition);

                        tmp = this.maxAgeInMs - (currentTime - connection.getConnectionCreationTimeInMs());

                        if (tmp < nextCheckInMs) {
                            nextCheckInMs = tmp;
                        }

                        if (connection.isExpired(currentTime)) {
                            // kill off this connection
                            closeConnection(connection);
                            continue;
                        }


                        if (this.lifoMode) {
                            // we can't put it back normally or it will end up in front again.
                            if (!(connection.getOriginatingPartition().getFreeConnections().offer(connection))) {
                                connection.internalClose();
                            }
                        } else {
                            this.pool.putConnectionBackInPartition(connection);
                        }


                        Thread.sleep(20L); // test slowly, this is not an operation that we're in a hurry to deal with (avoid CPU spikes)...
                    }
                } catch (Throwable e) {
                    //if (this.scheduler.isShutdown()) {
                    //logger.debug("Shutting down connection max age thread.");
                    // } else {
                    //logger.error("Connection max age thread exception.", e);
                    // }
                }

            } // throw it back on the queue

            if (!this.scheduler.isShutdown()) {
                this.scheduler.schedule(this, nextCheckInMs, TimeUnit.MILLISECONDS);
            }

        }


        /**
         * Closes off this connection
         *
         * @param connection to close
         */
        protected void closeConnection(ConnectionHandle connection) {
            if (connection != null) {
                try {
                    connection.internalClose();
                } catch (Throwable t) {
                    //logger.error("Destroy connection exception", t);
                } finally {
                    this.pool.postDestroyConnection(connection);
                }
            }
        }
    }

    class ConnectionPingThread implements Runnable {

        /**
         * Connections used less than this time ago are not keep-alive tested.
         */
        private long idleConnectionTestPeriodInMs;
        /**
         * Max no of ms to wait before a connection that isn't used is killed off.
         */
        private long idleMaxAgeInMs;
        /**
         * Partition being handled.
         */
        private ConnectionPartition partition;
        /**
         * Scheduler handle. *
         */
        private ScheduledExecutorService scheduler;
        /**
         * Handle to connection pool.
         */
        private Pool pool;
        /**
         * If true, we're operating in a LIFO fashion.
         */
        private boolean lifoMode;

        /**
         * Constructor
         *
         * @param connectionPartition          partition to work on
         * @param scheduler                    Scheduler handler.
         * @param pool                         pool handle
         * @param idleMaxAgeInMs               Threads older than this are killed off
         * @param idleConnectionTestPeriodInMs Threads that are idle for more than this time are sent a keep-alive.
         * @param lifoMode                     if true, we're running under a lifo fashion.
         */
        protected ConnectionPingThread(ConnectionPartition connectionPartition, ScheduledExecutorService scheduler,
                                         Pool pool, long idleMaxAgeInMs, long idleConnectionTestPeriodInMs, boolean lifoMode) {
            this.partition = connectionPartition;
            this.scheduler = scheduler;
            this.idleMaxAgeInMs = idleMaxAgeInMs;
            this.idleConnectionTestPeriodInMs = idleConnectionTestPeriodInMs;
            this.pool = pool;
            this.lifoMode = lifoMode;
        }

        public void run() {
            ConnectionHandle connection;
            long tmp;
            try {
                long nextCheckInMs = this.idleConnectionTestPeriodInMs;
                if (this.idleMaxAgeInMs > 0) {
                    if (this.idleConnectionTestPeriodInMs == 0) {
                        nextCheckInMs = this.idleMaxAgeInMs;
                    } else {
                        nextCheckInMs = Math.min(nextCheckInMs, this.idleMaxAgeInMs);
                    }
                }

                int partitionSize = this.partition.getAvailableConnections();
                long currentTimeInMs = System.currentTimeMillis();
                // go thru all partitions
                for (int i = 0; i < partitionSize; i++) {
                    // grab connections one by one.
                    connection = this.partition.getFreeConnections().poll();
                    if (connection != null) {
                        connection.setOriginatingPartition(this.partition);

                        // check if connection has been idle for too long (or is marked as broken)
                        if (connection.isPossiblyBroken() ||
                                ((this.idleMaxAgeInMs > 0) && (System.currentTimeMillis() - connection.getConnectionLastUsedInMs() > this.idleMaxAgeInMs))) {
                            // kill off this connection - it's broken or it has been idle for too long
                            closeConnection(connection);
                            continue;
                        }

                        // check if it's time to send a new keep-alive test statement.
                        if (this.idleConnectionTestPeriodInMs > 0 && (currentTimeInMs - connection.getConnectionLastUsedInMs() > this.idleConnectionTestPeriodInMs) &&
                                (currentTimeInMs - connection.getConnectionLastResetInMs() >= this.idleConnectionTestPeriodInMs)) {
                            // send a keep-alive, close off connection if we fail.
                            if (!this.pool.isConnectionHandleAlive(connection)) {
                                closeConnection(connection);
                                continue;
                            }
                            // calculate the next time to wake up
                            tmp = this.idleConnectionTestPeriodInMs;
                            if (this.idleMaxAgeInMs > 0) { // wake up earlier for the idleMaxAge test?
                                tmp = Math.min(tmp, this.idleMaxAgeInMs);
                            }
                        } else {
                            // determine the next time to wake up (connection test time or idle Max age?)
                            tmp = Math.abs(this.idleConnectionTestPeriodInMs - (currentTimeInMs - connection.getConnectionLastResetInMs()));
                            long tmp2 = Math.abs(this.idleMaxAgeInMs - (currentTimeInMs - connection.getConnectionLastUsedInMs()));
                            if (this.idleMaxAgeInMs > 0) {
                                tmp = Math.min(tmp, tmp2);
                            }

                        }
                        if (tmp < nextCheckInMs) {
                            nextCheckInMs = tmp;
                        }

                        if (this.lifoMode) {
                            // we can't put it back normally or it will end up in front again.
                            if (!(connection.getOriginatingPartition().getFreeConnections().offer(connection))) {
                                connection.internalClose();
                            }
                        } else {
                            this.pool.putConnectionBackInPartition(connection);
                        }

                        Thread.sleep(20L); // test slowly, this is not an operation that we're in a hurry to deal with (avoid CPU spikes)...
                    }

                }
                this.scheduler.schedule(this, nextCheckInMs, TimeUnit.MILLISECONDS);
            } catch (Throwable e) {
                if (this.scheduler.isShutdown()) {
                    // logger.debug("Shutting down connection tester thread.");
                } else {
                    logger.error("Connection ping thread interrupted", e);
                }
            }
        }


        /**
         * Closes off this connection
         *
         * @param connection to close
         */
        protected void closeConnection(ConnectionHandle connection) {

            if (connection != null && !connection.isClosed()) {
                try {
                    connection.internalClose();
                } catch (SQLException e) {
                    // logger.error("Destroy connection exception", e);
                } finally {
                    this.pool.postDestroyConnection(connection);
                    connection.getOriginatingPartition().getPoolWatchThreadSignalQueue().offer(new Object()); // item being pushed is not important.
                }
            }
        }


    }
}
