package org.xbib.io.unix;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Fifo {

    private final static Logger logger = LogManager.getLogger(Fifo.class.getName());

    public static native int mkfifo(String path, int flags) throws LastErrorException;

    public static native int close(int fd) throws LastErrorException;

    public static native int unlink(String path) throws LastErrorException;

    public static native String strerror(int errno);

    private static boolean loaded;

    static {
        if (!loaded) {
            if (Platform.isSolaris()) {
                System.loadLibrary("nsl");
                System.loadLibrary("socket");
            }
            if (!Platform.isWindows() && !Platform.isWindowsCE()) {
                try {
                    Native.register("c");
                } catch (NoClassDefFoundError e) {
                    logger.info("JNA not found, native methods will be disabled");
                } catch (UnsatisfiedLinkError e) {
                    logger.info("unable to link C library, native methods will be disabled");
                } catch (NoSuchMethodError e) {
                    logger.warn("unable to register C library");
                }
            } else {
                logger.warn("not able to create fifo on windows");
            }
            loaded = true;
        }
    }

    private String path;

    private int fd;

    public int openPipe(String path) throws IOException {
        try {
            this.path = path;
            fd = mkfifo(path, 0777);
            if (fd < 0) {
                throw new IOException(strerror(Native.getLastError()));
            }
            return fd;
        } catch (LastErrorException lee) {
            throw new IOException("native mkfifo() failed : " + strerror(lee.getErrorCode()));
        }
    }

    public String getPath() {
        return path;
    }

    public int closePipe() throws IOException {
        try {
            return close(fd);
        } catch (LastErrorException lee) {
            throw new IOException("native close() failed : " + strerror(lee.getErrorCode()));
        }
    }

    public int removePipe(String path) throws IOException {
        try {
            return unlink(path);
        } catch (LastErrorException lee) {
            if (lee.getErrorCode() != 2 /* ENOENT */) {
                throw new IOException("native unlink() failed : " + strerror(lee.getErrorCode()));
            } else {
                return 0;
            }
        }
    }

}
