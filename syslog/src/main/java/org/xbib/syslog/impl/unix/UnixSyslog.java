package org.xbib.syslog.impl.unix;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import org.xbib.syslog.SyslogMessageProcessorIF;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.AbstractSyslog;
import org.xbib.syslog.impl.AbstractSyslogWriter;
import org.xbib.syslog.util.OSDetectUtility;

/**
 * UnixSyslog is an extension of AbstractSyslog that provides support for
 * Unix-based syslog clients.
 * <p>
 * <p>This class requires the JNA (Java Native Access) library to directly
 * access the native C libraries utilized on Unix platforms.</p>
 * <p>
 */
public class UnixSyslog extends AbstractSyslog {

    protected UnixSyslogConfig unixSyslogConfig = null;

    protected interface CLibrary extends Library {
        public void openlog(final Memory ident, int option, int facility);

        public void syslog(int priority, final String format, final String message);

        public void closelog();
    }

    protected static int currentFacility = -1;
    protected static boolean openlogCalled = false;

    protected static CLibrary libraryInstance = null;

    protected static synchronized void loadLibrary(UnixSyslogConfig config) throws SyslogRuntimeException {
        if (!OSDetectUtility.isUnix()) {
            throw new SyslogRuntimeException("UnixSyslog not supported on non-Unix platforms");
        }

        if (libraryInstance == null) {
            libraryInstance = (CLibrary) Native.loadLibrary(config.getLibrary(), CLibrary.class);
        }
    }

    public void initialize() throws SyslogRuntimeException {
        try {
            this.unixSyslogConfig = (UnixSyslogConfig) this.syslogConfig;

        } catch (ClassCastException cce) {
            throw new SyslogRuntimeException("config must be of type UnixSyslogConfig");
        }

        loadLibrary(this.unixSyslogConfig);
    }

    protected static void write(int level, String message, UnixSyslogConfig config) throws SyslogRuntimeException {
        synchronized (libraryInstance) {
            if (currentFacility != config.getFacility()) {
                if (openlogCalled) {
                    libraryInstance.closelog();
                    openlogCalled = false;
                }

                currentFacility = config.getFacility();
            }

            if (!openlogCalled) {
                String ident = config.getIdent();

                if (ident != null && "".equals(ident.trim())) {
                    ident = null;
                }

                Memory identBuffer = null;

                if (ident != null) {
                    identBuffer = new Memory(128);
                    identBuffer.setString(0, ident, "UTF-8");
                }

                libraryInstance.openlog(identBuffer, config.getOption(), currentFacility);
                openlogCalled = true;
            }

            int priority = currentFacility | level;

            libraryInstance.syslog(priority, "%s", message);
        }
    }

    protected void write(int level, byte[] message) throws SyslogRuntimeException {
        // NO-OP
    }

    public void log(SyslogMessageProcessorIF messageProcessor, int level, String message) {
        write(level, message, this.unixSyslogConfig);
    }

    public void flush() throws SyslogRuntimeException {
        synchronized (libraryInstance) {
            libraryInstance.closelog();
            openlogCalled = false;
        }
    }

    public void shutdown() throws SyslogRuntimeException {
        flush();
    }

    public AbstractSyslogWriter getWriter() {
        return null;
    }

    public void returnWriter(AbstractSyslogWriter syslogWriter) {
        //
    }
}
