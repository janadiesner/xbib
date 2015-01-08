package org.xbib.io.ftp.sauron;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.io.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class FtpTest {

    private final static Logger logger = LogManager.getLogger(FtpTest.class);


    public void test() throws IOException {
        FTPConnectionFactory factory = new FTPConnectionFactory();
        FTPConnection connection = factory.getConnection(URI.create("ftp://ftp.tu-chemnitz.de/"));
        FTPSession session = connection.createSession();
        session.open(Session.Mode.READ);
        for (String path : session.list(".")) {
            logger.info("{}", path);
            File file = new File("/var/tmp/ftp/" + path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                FileOutputStream out = new FileOutputStream(file);
                session.download(path, out);
                out.close();
            }
        }
        session.close();
    }


}
