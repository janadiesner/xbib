package org.xbib.io.posix;

import com.sun.jna.FromNativeConverter;
import com.sun.jna.ToNativeConverter;
import com.sun.jna.TypeMapper;
import org.xbib.io.posix.freebsd.FreeBSDPOSIX;
import org.xbib.io.posix.linux.LinuxPOSIX;
import org.xbib.io.posix.macosx.MacOSPOSIX;
import org.xbib.io.posix.openbsd.OpenBSDPOSIX;
import org.xbib.io.posix.solaris.SolarisPOSIX;

class POSIXTypeMapper implements TypeMapper {
    public static final TypeMapper INSTANCE = new POSIXTypeMapper();

    private POSIXTypeMapper() {
    }

    public FromNativeConverter getFromNativeConverter(Class klazz) {
        if (Passwd.class.isAssignableFrom(klazz)) {
            if (Platform.IS_MAC) {
                return MacOSPOSIX.PASSWD;
            } else if (Platform.IS_LINUX) {
                return LinuxPOSIX.PASSWD;
            } else if (Platform.IS_SOLARIS) {
                return SolarisPOSIX.PASSWD;
            } else if (Platform.IS_FREEBSD) {
                return FreeBSDPOSIX.PASSWD;
            } else if (Platform.IS_OPENBSD) {
                return OpenBSDPOSIX.PASSWD;
            }
            return null;
        } else if (Group.class.isAssignableFrom(klazz)) {
            return BaseNativePOSIX.GROUP;
        }

        return null;
    }

    public ToNativeConverter getToNativeConverter(Class klazz) {
        return null;
    }
}
