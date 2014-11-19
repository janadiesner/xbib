package org.xbib.io.posix;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.xbib.io.posix.freebsd.FreeBSDPOSIX;
import org.xbib.io.posix.linux.LinuxLibC;
import org.xbib.io.posix.linux.LinuxPOSIX;
import org.xbib.io.posix.macosx.MacOSPOSIX;
import org.xbib.io.posix.openbsd.OpenBSDPOSIX;
import org.xbib.io.posix.solaris.SolarisPOSIX;

import java.util.HashMap;
import java.util.Map;

public class POSIXFactory {

    static final String LIBC = Platform.IS_LINUX ? "libc.so.6" : "c";

    static LibC libc = null;

    static final Map<Object, Object> defaultOptions = new HashMap<Object, Object>() {{
        put(Library.OPTION_TYPE_MAPPER, POSIXTypeMapper.INSTANCE);
    }};

    public static POSIX getPOSIX(POSIXHandler handler, boolean useNativePOSIX) {
        POSIX posix = null;

        if (useNativePOSIX) {
            try {
                if (Platform.IS_MAC) {
                    posix = loadMacOSPOSIX(handler);
                } else if (Platform.IS_LINUX) {
                    posix = loadLinuxPOSIX(handler);
                } else if (Platform.IS_FREEBSD) {
                    posix = loadFreeBSDPOSIX(handler);
                } else if (Platform.IS_OPENBSD) {
                    posix = loadOpenBSDPOSIX(handler);
                } else if (Platform.IS_32_BIT) {
                    if (Platform.IS_SOLARIS) {
                        posix = loadSolarisPOSIX(handler);
                    }
                }
                if (handler.isVerbose()) {
                    if (posix != null) {
                        System.err.println("Successfully loaded native POSIX impl.");
                    } else {
                        System.err.println("Failed to load native POSIX impl; falling back on Java impl. Unsupported OS.");
                    }
                }
            } catch (Throwable t) {
                if (handler.isVerbose()) {
                    System.err.println("Failed to load native POSIX impl; falling back on Java impl. Stacktrace follows.");
                    t.printStackTrace();
                }
            }
        }
        return posix;
    }

    public static POSIX loadLinuxPOSIX(POSIXHandler handler) {
        return new LinuxPOSIX(LIBC, loadLibC(LIBC, LinuxLibC.class, defaultOptions), handler);
    }

    public static POSIX loadMacOSPOSIX(POSIXHandler handler) {
        return new MacOSPOSIX(LIBC, loadLibC(LIBC, LibC.class, defaultOptions), handler);
    }

    public static POSIX loadSolarisPOSIX(POSIXHandler handler) {
        return new SolarisPOSIX(LIBC, loadLibC(LIBC, LibC.class, defaultOptions), handler);
    }

    public static POSIX loadFreeBSDPOSIX(POSIXHandler handler) {
        return new FreeBSDPOSIX(LIBC, loadLibC(LIBC, LibC.class, defaultOptions), handler);
    }

    public static POSIX loadOpenBSDPOSIX(POSIXHandler handler) {
        return new OpenBSDPOSIX(LIBC, loadLibC(LIBC, LibC.class, defaultOptions), handler);
    }

    public static LibC loadLibC(String libraryName, Class<?> libCClass, Map<Object, Object> options) {
        if (libc != null) {
            return libc;
        }
        libc = (LibC) Native.loadLibrary(libraryName, libCClass, options);
        return libc;
    }


}
