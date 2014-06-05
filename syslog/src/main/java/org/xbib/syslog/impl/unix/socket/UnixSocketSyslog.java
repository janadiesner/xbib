package org.xbib.syslog.impl.unix.socket;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.AbstractSyslog;
import org.xbib.syslog.impl.AbstractSyslogWriter;
import org.xbib.syslog.util.OSDetectUtility;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * UnixSocketSyslog is an extension of AbstractSyslog that provides support for
 * Unix socket-based syslog clients.
 * <p>
 * <p>This class requires the JNA (Java Native Access) library to directly
 * access the native C libraries utilized on Unix platforms.</p>
 */
public class UnixSocketSyslog extends AbstractSyslog {

    protected static class SockAddr extends Structure {
        public final static int SUN_PATH_SIZE = 108;
        public final static byte[] ZERO_BYTE = new byte[]{0};

        public short sun_family = 1;
        public byte[] sun_path = new byte[SUN_PATH_SIZE];

        public void setSunPath(String sunPath) {
            System.arraycopy(sunPath.getBytes(), 0, this.sun_path, 0, sunPath.length());
            System.arraycopy(ZERO_BYTE, 0, this.sun_path, sunPath.length(), 1);
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("sun_family", "sun_path");
        }
    }

    protected interface CLibrary extends Library {
        public int socket(int domain, int type, int protocol);

        public int connect(int sockfd, SockAddr sockaddr, int addrlen);

        public int write(int fd, ByteBuffer buffer, int count);

        public int close(int fd);

        public String strerror(int errno);
    }

    protected boolean libraryLoaded = false;
    protected CLibrary libraryInstance = null;

    protected UnixSocketSyslogConfig unixSocketSyslogConfig = null;
    protected int fd = -1;

    protected synchronized void loadLibrary() {
        if (!OSDetectUtility.isUnix()) {
            throw new SyslogRuntimeException("UnixSyslog not supported on non-Unix platforms");
        }

        if (!this.libraryLoaded) {
            this.libraryInstance = (CLibrary) Native.loadLibrary(this.unixSocketSyslogConfig.getLibrary(), CLibrary.class);
            this.libraryLoaded = true;
        }
    }

    public void initialize() throws SyslogRuntimeException {
        try {
            this.unixSocketSyslogConfig = (UnixSocketSyslogConfig) this.syslogConfig;

        } catch (ClassCastException cce) {
            throw new SyslogRuntimeException("config must be of type UnixSocketSyslogConfig");
        }

        loadLibrary();

    }

    protected synchronized void connect() {
        if (this.fd != -1) {
            return;
        }

        this.fd = this.libraryInstance.socket(this.unixSocketSyslogConfig.getFamily(), this.unixSocketSyslogConfig.getType(), this.unixSocketSyslogConfig.getProtocol());

        if (this.fd == -1) {
            this.fd = -1;
            return;
        }

        SockAddr sockAddr = new SockAddr();

        sockAddr.sun_family = this.unixSocketSyslogConfig.getFamily();
        sockAddr.setSunPath(this.unixSocketSyslogConfig.getPath());

        int c = this.libraryInstance.connect(this.fd, sockAddr, sockAddr.size());

        if (c == -1) {
            this.fd = -1;
        }
    }

    protected void write(int level, byte[] message) throws SyslogRuntimeException {
        if (this.fd == -1) {
            connect();
        }

        if (this.fd == -1) {
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(message);

        this.libraryInstance.write(this.fd, byteBuffer, message.length);
    }

    public void flush() throws SyslogRuntimeException {
        shutdown();

        this.fd = this.libraryInstance.socket(this.unixSocketSyslogConfig.getFamily(), this.unixSocketSyslogConfig.getType(), this.unixSocketSyslogConfig.getProtocol());
    }

    public void shutdown() throws SyslogRuntimeException {
        if (this.fd == -1) {
            return;
        }

        this.libraryInstance.close(this.fd);

        this.fd = -1;
    }

    public AbstractSyslogWriter getWriter() {
        return null;
    }

    public void returnWriter(AbstractSyslogWriter syslogWriter) {
        //
    }
}
