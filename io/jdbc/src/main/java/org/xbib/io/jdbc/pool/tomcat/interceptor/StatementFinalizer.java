
package org.xbib.io.jdbc.pool.tomcat.interceptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.jdbc.pool.tomcat.ConnectionPool;
import org.xbib.io.jdbc.pool.tomcat.PooledConnection;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Keeps track of statements associated with a connection and invokes close upon {@link java.sql.Connection#close()}
 * Useful for applications that dont close the associated statements after being done with a connection.
 *
 */
public class StatementFinalizer extends AbstractCreateStatementInterceptor {

    private static final Logger log = LogManager.getLogger(StatementFinalizer.class.getSimpleName());

    protected ArrayList<WeakReference<Statement>> statements = new ArrayList<WeakReference<Statement>>();

    @Override
    public Object createStatement(Object proxy, Method method, Object[] args, Object statement, long time) {
        try {
            if (statement instanceof Statement) {
                statements.add(new WeakReference<Statement>((Statement) statement));
            }
        } catch (ClassCastException x) {
            //ignore this one
        }
        return statement;
    }

    @Override
    public void closeInvoked() {
        while (statements.size() > 0) {
            WeakReference<Statement> ws = statements.remove(0);
            Statement st = ws.get();
            if (st != null) {
                try {
                    st.close();
                } catch (Exception ignore) {
                    if (log.isDebugEnabled()) {
                        log.debug("Unable to closed statement upon connection close.", ignore);
                    }
                }
            }
        }
    }

    @Override
    public void reset(ConnectionPool parent, PooledConnection con) {
        statements.clear();
        super.reset(parent, con);
    }


}
