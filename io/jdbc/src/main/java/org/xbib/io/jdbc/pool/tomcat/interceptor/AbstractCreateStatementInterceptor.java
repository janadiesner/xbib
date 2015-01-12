
package org.xbib.io.jdbc.pool.tomcat.interceptor;

import org.xbib.io.jdbc.pool.tomcat.ConnectionPool;
import org.xbib.io.jdbc.pool.tomcat.JdbcInterceptor;
import org.xbib.io.jdbc.pool.tomcat.PooledConnection;

import java.lang.reflect.Method;

/**
 * Abstraction interceptor. This component intercepts all calls to create some type of SQL statement.
 * By extending this class, one can intercept queries and update statements by overriding the {@link #createStatement(Object, java.lang.reflect.Method, Object[], Object, long)}
 * method.
 */
public abstract class AbstractCreateStatementInterceptor extends JdbcInterceptor {
    protected static final String CREATE_STATEMENT = "createStatement";
    protected static final int CREATE_STATEMENT_IDX = 0;
    protected static final String PREPARE_STATEMENT = "prepareStatement";
    protected static final int PREPARE_STATEMENT_IDX = 1;
    protected static final String PREPARE_CALL = "prepareCall";
    protected static final int PREPARE_CALL_IDX = 2;

    protected static final String[] STATEMENT_TYPES = {CREATE_STATEMENT, PREPARE_STATEMENT, PREPARE_CALL};
    protected static final int STATEMENT_TYPE_COUNT = STATEMENT_TYPES.length;

    protected static final String EXECUTE = "execute";
    protected static final String EXECUTE_QUERY = "executeQuery";
    protected static final String EXECUTE_UPDATE = "executeUpdate";
    protected static final String EXECUTE_BATCH = "executeBatch";

    protected static final String[] EXECUTE_TYPES = {EXECUTE, EXECUTE_QUERY, EXECUTE_UPDATE, EXECUTE_BATCH};

    public AbstractCreateStatementInterceptor() {
        super();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (compare(CLOSE_VAL, method)) {
            closeInvoked();
            return super.invoke(proxy, method, args);
        } else {
            boolean process = false;
            process = isStatement(method, process);
            if (process) {
                long start = System.currentTimeMillis();
                Object statement = super.invoke(proxy, method, args);
                long delta = System.currentTimeMillis() - start;
                return createStatement(proxy, method, args, statement, delta);
            } else {
                return super.invoke(proxy, method, args);
            }
        }
    }

    /**
     * This method will be invoked after a successful statement creation. This method can choose to return a wrapper
     * around the statement or return the statement itself.
     * If this method returns a wrapper then it should return a wrapper object that implements one of the following interfaces.
     * {@link java.sql.Statement}, {@link java.sql.PreparedStatement} or {@link java.sql.CallableStatement}
     *
     * @param proxy     the actual proxy object
     * @param method    the method that was called. It will be one of the methods defined in {@link #STATEMENT_TYPES}
     * @param args      the arguments to the method
     * @param statement the statement that the underlying connection created
     * @return a {@link java.sql.Statement} object
     */
    public abstract Object createStatement(Object proxy, Method method, Object[] args, Object statement, long time);

    /**
     * Method invoked when the operation {@link java.sql.Connection#close()} is invoked.
     */
    public abstract void closeInvoked();

    /**
     * Returns true if the method that is being invoked matches one of the statement types.
     *
     * @param method  the method being invoked on the proxy
     * @param process boolean result used for recursion
     * @return returns true if the method name matched
     */
    protected boolean isStatement(Method method, boolean process) {
        return process(STATEMENT_TYPES, method, process);
    }

    /**
     * Returns true if the method that is being invoked matches one of the execute types.
     *
     * @param method  the method being invoked on the proxy
     * @param process boolean result used for recursion
     * @return returns true if the method name matched
     */
    protected boolean isExecute(Method method, boolean process) {
        return process(EXECUTE_TYPES, method, process);
    }

    /*
     * Returns true if the method that is being invoked matches one of the method names passed in
     * @param names list of method names that we want to intercept
     * @param method the method being invoked on the proxy
     * @param process boolean result used for recursion
     * @return returns true if the method name matched
     */
    protected boolean process(String[] names, Method method, boolean process) {
        final String name = method.getName();
        for (int i = 0; (!process) && i < names.length; i++) {
            process = compare(names[i], name);
        }
        return process;
    }

    @Override
    public void reset(ConnectionPool parent, PooledConnection con) {
    }
}