
package org.xbib.io.jdbc.pool.tomcat.jmx;

import org.xbib.io.jdbc.pool.tomcat.PoolConfiguration;

public interface ConnectionPoolMBean extends PoolConfiguration {


    public int getSize();

    public int getIdle();

    public int getActive();

    public int getNumIdle();

    public int getNumActive();

    public int getWaitCount();

    public void checkIdle();

    public void checkAbandoned();

    public void testIdle();

    /**
     * Purges all connections in the pool.
     * For connections currently in use, these connections will be
     * purged when returned on the pool. This call also
     * purges connections that are idle and in the pool
     * To only purge used/active connections see {@link #purgeOnReturn()}
     */
    public void purge();

    /**
     * Purges connections when they are returned from the pool.
     * This call does not purge idle connections until they are used.
     * To purge idle connections see {@link #purge()}
     */
    public void purgeOnReturn();

}
