
package org.xbib.io.jdbc.pool.bonecp;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Statistics class
 */
public class Statistics {
    /**
     * No of cache hits.
     */
    private final AtomicLong cacheHits = new AtomicLong(0);
    /**
     * No of cache misses.
     */
    private final AtomicLong cacheMiss = new AtomicLong(0);
    /**
     * No of statements cached.
     */
    private final AtomicLong statementsCached = new AtomicLong(0);
    /**
     * Connections obtained.
     */
    private final AtomicLong connectionsRequested = new AtomicLong(0);
    /**
     * Time taken to give a connection to the application.
     */
    private final AtomicLong cumulativeConnectionWaitTime = new AtomicLong(0);
    /**
     * Time taken to execute statements.
     */
    private final AtomicLong cumulativeStatementExecuteTime = new AtomicLong(0);
    /**
     * Time taken to prepare statements (or obtain from cache).
     */
    private final AtomicLong cumulativeStatementPrepareTime = new AtomicLong(0);
    /**
     * Number of statements that have been executed.
     */
    private final AtomicLong statementsExecuted = new AtomicLong(0);
    /**
     * Number of statements that have been prepared.
     */
    private final AtomicLong statementsPrepared = new AtomicLong(0);

    private Pool pool;

    public Statistics(Pool pool) {
        this.pool = pool;
    }

    public void resetStats() {
        this.cacheHits.set(0);
        this.cacheMiss.set(0);
        this.statementsCached.set(0);
        this.connectionsRequested.set(0);
        this.cumulativeConnectionWaitTime.set(0);
        this.cumulativeStatementExecuteTime.set(0);
        this.cumulativeStatementPrepareTime.set(0);
        this.statementsExecuted.set(0);
        this.statementsPrepared.set(0);
    }

    public double getConnectionWaitTimeAvg() {
        return this.connectionsRequested.get() == 0 ? 0 : this.cumulativeConnectionWaitTime.get() / (1.0 * this.connectionsRequested.get()) / 1000000.0;
    }

    public double getStatementExecuteTimeAvg() {
        return this.statementsExecuted.get() == 0 ? 0 : this.cumulativeStatementExecuteTime.get() / (1.0 * this.statementsExecuted.get()) / 1000000.0;
    }

    public double getStatementPrepareTimeAvg() {
        return this.cumulativeStatementPrepareTime.get() == 0 ? 0 : this.cumulativeStatementPrepareTime.get() / (1.0 * this.statementsPrepared.get()) / 1000000.0;
    }

    public int getTotalLeased() {
        return this.pool.getTotalLeased();
    }

    public int getTotalFree() {
        return this.pool.getTotalFree();
    }

    public int getTotalCreatedConnections() {
        return this.pool.getTotalCreatedConnections();
    }

    public long getCacheHits() {
        return this.cacheHits.get();
    }

    public long getCacheMiss() {
        return this.cacheMiss.get();
    }

    public long getStatementsCached() {
        return this.statementsCached.get();
    }

    public long getConnectionsRequested() {
        return this.connectionsRequested.get();
    }

    public long getCumulativeConnectionWaitTime() {
        return this.cumulativeConnectionWaitTime.get() / 1000000;
    }

    public void addCumulativeConnectionWaitTime(long increment) {
        this.cumulativeConnectionWaitTime.addAndGet(increment);
    }

    public void incrementStatementsExecuted() {
        this.statementsExecuted.incrementAndGet();
    }

    public void incrementStatementsPrepared() {
        this.statementsPrepared.incrementAndGet();
    }

    public void incrementStatementsCached() {
        this.statementsCached.incrementAndGet();
    }

    public void incrementCacheMiss() {
        this.cacheMiss.incrementAndGet();
    }

    public void incrementCacheHits() {
        this.cacheHits.incrementAndGet();
    }

    public void incrementConnectionsRequested() {
        this.connectionsRequested.incrementAndGet();
    }

    public double getCacheHitRatio() {
        return this.cacheHits.get() + this.cacheMiss.get() == 0 ? 0 : this.cacheHits.get() / (1.0 * this.cacheHits.get() + this.cacheMiss.get());
    }

    public long getStatementsExecuted() {
        return this.statementsExecuted.get();
    }

    public long getCumulativeStatementExecutionTime() {
        return this.cumulativeStatementExecuteTime.get() / 1000000;
    }

    public void addStatementExecuteTime(long time) {
        this.cumulativeStatementExecuteTime.addAndGet(time);
    }

    public void addStatementPrepareTime(long time) {
        this.cumulativeStatementPrepareTime.addAndGet(time);
    }

    public long getCumulativeStatementPrepareTime() {
        return this.cumulativeStatementPrepareTime.get() / 1000000;
    }

    public long getStatementsPrepared() {
        return this.statementsPrepared.get();
    }

}
