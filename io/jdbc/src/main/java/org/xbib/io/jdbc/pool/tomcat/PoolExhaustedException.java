
package org.xbib.io.jdbc.pool.tomcat;

import java.sql.SQLException;

public class PoolExhaustedException extends SQLException {

    public PoolExhaustedException(String reason) {
        super(reason);
    }

}
