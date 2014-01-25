
package org.xbib.tools.aleph;

import org.xbib.common.settings.Settings;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Iterate over Aleph Z00P records
 */
public class AlephPublishIterator implements Iterator<Integer>, Closeable {

    private final static Logger logger = LoggerFactory.getLogger(AlephPublishIterator.class.getName());

    private PreparedStatement statement;

    private ResultSet results;

    private String id;

    public AlephPublishIterator() {
    }

    public void configure(Settings settings, Connection connection) {
        try {
            String query = "select /*+ index(z00p z00p_id5) */ z00p_doc_number from "
                    + settings.get("library")
                    + ".z00p where z00p_set = '" + settings.get("set")
                    + "' and z00p_timestamp between '"
                    + settings.get("from") + "0000000"
                    + "' and '" +settings.get("to") + "0000000" + "'";
            this.statement = connection.prepareStatement(query);
            this.results = statement.executeQuery();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasNext() {
        if (results == null) {
            return false;
        }
        try {
            if (results.next()) {
                this.id = results.getString(1);
                if (logger.isTraceEnabled()) {
                     logger.trace("next sys id = {}", id);
                }
                return true;
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("no more results");
                }
                results.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            try {
                results.close();
                results = null;
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public Integer next() {
        if (results == null) {
            return null;
        }
        if (id != null) {
            return Integer.valueOf(id);
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

    @Override
    public void close() throws IOException {
        try {
            if (results != null) {
                results.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
