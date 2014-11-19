package org.xbib.io.posix.freebsd;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import org.xbib.io.posix.BaseNativePOSIX;
import org.xbib.io.posix.FileStat;
import org.xbib.io.posix.LibC;
import org.xbib.io.posix.POSIXHandler;

public final class FreeBSDPOSIX extends BaseNativePOSIX {
    public FreeBSDPOSIX(String libraryName, LibC libc, POSIXHandler handler) {
        super(libraryName, libc, handler);
    }

    public FileStat allocateStat() {
        return new FreeBSDHeapFileStat(this);
    }

    public static final PointerConverter PASSWD = new PointerConverter() {
        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new FreeBSDPasswd((Pointer) arg) : null;
        }
    };
}
