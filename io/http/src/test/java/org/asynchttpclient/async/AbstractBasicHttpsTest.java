package org.asynchttpclient.async;

import static org.asynchttpclient.async.util.TestUtils.*;

import org.testng.annotations.BeforeClass;

public abstract class AbstractBasicHttpsTest extends AbstractBasicTest {

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        server = newJettyHttpsServer(port1);
        server.setHandler(configureHandler());
        server.start();
        logger.info("Local HTTP server started successfully");
    }
}
