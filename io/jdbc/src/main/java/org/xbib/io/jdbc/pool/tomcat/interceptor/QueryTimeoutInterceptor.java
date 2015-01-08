
package org.xbib.io.jdbc.pool.tomcat.interceptor;

import org.xbib.io.jdbc.pool.tomcat.PoolProperties.InterceptorProperty;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class QueryTimeoutInterceptor extends AbstractCreateStatementInterceptor {

    int timeout;

    @Override
    public void setProperties(Map<String, InterceptorProperty> properties) {
        super.setProperties(properties);
        timeout = properties.get("queryTimeout").getValueAsInt(-1);
    }

    @Override
    public Object createStatement(Object proxy, Method method, Object[] args, Object statement, long time) {
        if (statement instanceof Statement && timeout > 0) {
            Statement s = (Statement) statement;
            try {
                s.setQueryTimeout(timeout);
            } catch (SQLException x) {
                //log.warn("[QueryTimeoutInterceptor] Unable to set query timeout:" + x.getMessage(), x);
            }
        }
        return statement;
    }

    @Override
    public void closeInvoked() {
    }

}
