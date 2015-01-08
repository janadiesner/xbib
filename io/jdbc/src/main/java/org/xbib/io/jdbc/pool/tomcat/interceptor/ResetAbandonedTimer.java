
package org.xbib.io.jdbc.pool.tomcat.interceptor;

import org.xbib.io.jdbc.pool.tomcat.JdbcInterceptor;
import org.xbib.io.jdbc.pool.tomcat.PooledConnection;
import org.xbib.io.jdbc.pool.tomcat.ProxyConnection;

import java.lang.reflect.Method;

/**
 * Class that resets the abandoned timer on any activity on the
 * Connection or any successful query executions.
 * This interceptor is useful for when you have a {@link org.xbib.io.jdbc.pool.tomcat.PoolConfiguration#setRemoveAbandonedTimeout(int)}
 * that is fairly low, and you want to reset the abandoned time each time any operation on the connection is performed
 * This is useful for batch processing programs that use connections for extensive amount of times.

 */
public class ResetAbandonedTimer extends AbstractQueryReport {

    public ResetAbandonedTimer() {
    }

    public boolean resetTimer() {
        boolean result = false;
        JdbcInterceptor interceptor = this.getNext();
        while (interceptor != null && result == false) {
            if (interceptor instanceof ProxyConnection) {
                PooledConnection con = ((ProxyConnection) interceptor).getConnection();
                if (con != null) {
                    con.setTimestamp(System.currentTimeMillis());
                    result = true;
                } else {
                    break;
                }
            }
            interceptor = interceptor.getNext();
        }
        return result;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = super.invoke(proxy, method, args);
        resetTimer();
        return result;
    }

    @Override
    protected void prepareCall(String query, long time) {
        resetTimer();
    }

    @Override
    protected void prepareStatement(String sql, long time) {
        resetTimer();

    }

    @Override
    public void closeInvoked() {
        resetTimer();
    }

    @Override
    protected String reportQuery(String query, Object[] args, String name, long start, long delta) {
        resetTimer();
        return super.reportQuery(query, args, name, start, delta);
    }

    @Override
    protected String reportSlowQuery(String query, Object[] args, String name, long start, long delta) {
        resetTimer();
        return super.reportSlowQuery(query, args, name, start, delta);
    }
}
