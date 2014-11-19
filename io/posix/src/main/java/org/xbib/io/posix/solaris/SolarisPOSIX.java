package org.xbib.io.posix.solaris;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import org.xbib.io.posix.BaseNativePOSIX;
import org.xbib.io.posix.FileStat;
import org.xbib.io.posix.LibC;
import org.xbib.io.posix.POSIXHandler;

public class SolarisPOSIX extends BaseNativePOSIX {
    public SolarisPOSIX(String libraryName, LibC libc, POSIXHandler handler) {
        super(libraryName, libc, handler);
    }

    public FileStat allocateStat() {
        return new SolarisHeapFileStat(this);
    }

    @Override
    public int lchmod(String filename, int mode) {
        handler.unimplementedError("lchmod");

        return -1;
    }

    @Override
    public FileStat lstat(String path) {
        FileStat stat = allocateStat();

        if (libc.lstat64(path, stat) < 0) {
            handler.error(ERRORS.ENOENT, path);
        }

        return stat;
    }

    @Override
    public FileStat stat(String path) {
        FileStat stat = allocateStat();

        if (libc.stat64(path, stat) < 0) {
            handler.error(ERRORS.ENOENT, path);
        }

        return stat;
    }

    public static final PointerConverter PASSWD = new PointerConverter() {
        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new SolarisPasswd((Pointer) arg) : null;
        }
    };
}
