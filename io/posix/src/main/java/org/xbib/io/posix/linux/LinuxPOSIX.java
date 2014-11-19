package org.xbib.io.posix.linux;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import org.xbib.io.posix.BaseNativePOSIX;
import org.xbib.io.posix.FileStat;
import org.xbib.io.posix.LibC;
import org.xbib.io.posix.POSIXHandler;

public final class LinuxPOSIX extends BaseNativePOSIX {

    private final static boolean is32bit = "32".equals(System.getProperty("sun.arch.data.model", "32"));

    private final boolean hasFxstat;
    private final boolean hasLxstat;
    private final boolean hasXstat;
    private final boolean hasFstat;
    private final boolean hasLstat;
    private final boolean hasStat;
    private final int statVersion;

    public LinuxPOSIX(String libraryName, LibC libc, POSIXHandler handler) {
        super(libraryName, libc, handler);

        statVersion = is32bit ? 3 : 0;
        hasFxstat = hasMethod("__fxstat64");
        hasLxstat = hasMethod("__lxstat64");
        hasXstat = hasMethod("__xstat64");
        hasFstat = !hasFxstat && hasMethod("fstat64");
        hasLstat = !hasLxstat && hasMethod("lstat64");
        hasStat = !hasXstat && hasMethod("stat64");
    }

    @Override
    public FileStat allocateStat() {
        if (is32bit) {
            return new LinuxHeapFileStat(this);
        } else {
            return new Linux64HeapFileStat(this);
        }
    }

    @Override
    public FileStat lstat(String path) {
        if (!hasLxstat) {
            if (hasLstat) {
                return super.lstat(path);
            }
            handler.unimplementedError("lstat");
        }
        FileStat stat = allocateStat();
        if (((LinuxLibC) libc).__lxstat64(statVersion, path, stat) < 0) {
            handler.error(ERRORS.ENOENT, path);
        }
        return stat;
    }

    @Override
    public FileStat stat(String path) {
        if (!hasXstat) {
            if (hasStat) {
                return super.stat(path);
            }
            handler.unimplementedError("stat");
        }
        FileStat stat = allocateStat();
        if (((LinuxLibC) libc).__xstat64(statVersion, path, stat) < 0) {
            handler.error(ERRORS.ENOENT, path);
        }
        return stat;
    }

    public static final PointerConverter PASSWD = new PointerConverter() {
        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new LinuxPasswd((Pointer) arg) : null;
        }
    };
}
