package org.xbib.syslog.util;

/**
 * OSDetectUtility provides operating system detection used to determine
 * whether Syslog is running on a Unix platform.
 *
 */
public final class OSDetectUtility {
    private final static String[] UNIX_PLATFORMS = {
            "Linux",
            "Mac OS",
            "Solaris",
            "SunOS",
            "MPE/iX",
            "HP-UX",
            "AIX",
            "OS/390",
            "FreeBSD",
            "Irix",
            "Digital Unix",
            "FreeBSD",
            "OSF1",
            "OpenVMS"
    };

    private final static String[] WINDOWS_PLATFORMS = {
            "Windows",
            "OS/2"
    };

    private static boolean UNIX = false;
    private static boolean WINDOWS = false;

    private OSDetectUtility() {
        //
    }

    private static boolean isMatch(String[] platforms) {
        boolean match = false;

        String osName = System.getProperty("os.name");

        if (osName != null && !"".equals(osName.trim())) {
            osName = osName.toLowerCase();

            for (int i = 0; i < platforms.length; i++) {
                String platform = platforms[i].toLowerCase();

                if (osName.indexOf(platform) > -1) {
                    match = true;
                    break;
                }
            }
        }

        return match;
    }

    static {
        UNIX = isMatch(UNIX_PLATFORMS);
        WINDOWS = isMatch(WINDOWS_PLATFORMS);
    }

    public static boolean isUnix() {
        return UNIX;
    }

    public static boolean isWindows() {
        return WINDOWS;
    }
}
