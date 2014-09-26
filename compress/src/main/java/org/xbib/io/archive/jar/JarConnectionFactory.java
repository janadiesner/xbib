
package org.xbib.io.archive.jar;

import org.xbib.io.Connection;
import org.xbib.io.ConnectionFactory;
import org.xbib.io.archive.ArchiveSession;

import java.io.IOException;
import java.net.URI;

/**
 * Jar connection factory
 */
public final class JarConnectionFactory implements ConnectionFactory<JarSession> {

    @Override
    public String getName() {
        return "jar";
    }

    /**
     * Get connection
     *
     * @param uri the connection URI
     * @return a new connection
     * @throws java.io.IOException if connection can not be established
     */
    @Override
    public Connection<JarSession> getConnection(URI uri) throws IOException {
        JarConnection connection = new JarConnection();
        connection.setURI(uri);
        return connection;
    }

    @Override
    public boolean canOpen(URI uri) {
        return ArchiveSession.canOpen(uri, getName(), true);
    }
}
