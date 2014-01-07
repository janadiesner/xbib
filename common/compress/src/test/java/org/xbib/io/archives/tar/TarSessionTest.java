
package org.xbib.io.archives.tar;

import java.net.URI;
import org.testng.annotations.Test;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Session;
import org.xbib.io.ObjectPacket;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class TarSessionTest {

    private static final Logger logger = LoggerFactory.getLogger(TarSessionTest.class.getName());

    @Test
    public void readFromTar() throws Exception {
        URI uri = URI.create("file:src/test/resources/test.tar.bz2");
        Connection<Session> c = ConnectionService.getInstance()
                .getConnectionFactory(uri)
                .getConnection(uri);
        Session<ObjectPacket> session = c.createSession();
        session.open(Session.Mode.READ);
        ObjectPacket message = session.read();
        logger.info("name = {} object = {}",
                message.name(), message.packet());
        session.close();
        c.close();
    }
}
