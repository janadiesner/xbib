
package org.xbib.io.unix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.io.posix.DummyPOSIXHandler;
import org.xbib.io.posix.FileStat;
import org.xbib.io.posix.POSIX;
import org.xbib.io.posix.POSIXFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FifoTest {

    private final static Logger logger = LogManager.getLogger(FifoTest.class.getName());


    String path = "test_test.fifo";

    Runnable server = () -> {
        Fifo fifo = new Fifo();
        logger.info("server start");
        try {
            fifo.removePipe(path);
            fifo.openPipe(path);
            POSIX posix = POSIXFactory.getPOSIX(new DummyPOSIXHandler(), true);
            FileStat st = posix.stat(path);
            logger.info("stat ftype={}", st.ftype());

            while (true) {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
                byte[] buffer = new byte[1024];
                if (bis.available() > 0) {
                    bis.read(buffer);
                    logger.info(new String(buffer));
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                logger.info("close bis");
                bis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            try {
                fifo.closePipe();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        logger.info("server end");
    };

    Runnable client = () -> {
        Fifo fifo = new Fifo();
        logger.info("client start");
        try {
            while (!Thread.interrupted()) {
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));
                out.write("Hello World\n".getBytes());
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };


    public void test() throws InterruptedException {
        Thread serverThread = new Thread(server);
        serverThread.start();
        Thread.sleep(1000L);
        Thread clientThread = new Thread(client);
        clientThread.start();
        Thread.sleep(1000L);
        serverThread.interrupt();
        clientThread.interrupt();

    }
}
