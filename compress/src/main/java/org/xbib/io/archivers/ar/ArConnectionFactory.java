
package org.xbib.io.archivers.ar;

import org.xbib.io.Connection;
import org.xbib.io.ConnectionFactory;
import org.xbib.io.archivers.ArchiveSession;

import java.io.IOException;
import java.net.URI;

/**
 * Ar connection factory
 */
public final class ArConnectionFactory implements ConnectionFactory<ArSession> {

    public final static String SUFFIX = "ar";

    @Override
    public String getName() {
        return SUFFIX;
    }

    /**
     * Get connection
     *
     * @param uri the connection URI
     * @return a new connection
     * @throws java.io.IOException if connection can not be established
     */
    @Override
    public Connection<ArSession> getConnection(URI uri) throws IOException {
        ArConnection connection = new ArConnection();
        connection.setURI(uri);
        return connection;
    }

    @Override
    public boolean canOpen(URI uri) {
        return ArchiveSession.canOpen(uri, getName(), true);
    }


}
