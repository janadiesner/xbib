package org.xbib.io.posix;

import java.util.HashMap;
import java.util.Map;

public class Platform {
    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_NAME_LC = OS_NAME.toLowerCase();

    // Generic Windows designation
    private static final String WINDOWS = "windows";
    // For Windows 95, 98... Would these platforms actually work?
    private static final String WINDOWS_9X = "windows 9";
    // TODO: Windows ME?
    private static final String WINDOWS_NT = "nt";
    private static final String WINDOWS_20X = "windows 2";
    private static final String WINDOWS_XP = "windows xp";
    // TODO:  For Windows Server 2003, 2008
    private static final String WINDOWS_VISTA = "vista";
    private static final String MAC_OS = "mac os";
    private static final String DARWIN = "darwin";
    private static final String FREEBSD = "freebsd";
    private static final String OPENBSD = "openbsd";
    private static final String LINUX = "linux";
    private static final String SOLARIS = "sunos";

    // TODO: investigate supported platforms for OpenJDK7?

    public static final boolean IS_WINDOWS = OS_NAME_LC.contains(WINDOWS);
    public static final boolean IS_WINDOWS_9X = OS_NAME_LC.contains(WINDOWS_9X);
    public static final boolean IS_WINDOWS_NT = IS_WINDOWS && OS_NAME_LC.contains(WINDOWS_NT);
    public static final boolean IS_WINDOWS_20X = OS_NAME_LC.contains(WINDOWS_20X);
    public static final boolean IS_WINDOWS_XP = OS_NAME_LC.contains(WINDOWS_XP);
    public static final boolean IS_WINDOWS_VISTA = IS_WINDOWS && OS_NAME_LC.contains(WINDOWS_VISTA);
    public static final boolean IS_MAC = OS_NAME_LC.startsWith(MAC_OS) || OS_NAME_LC.startsWith(DARWIN);
    public static final boolean IS_FREEBSD = OS_NAME_LC.startsWith(FREEBSD);
    public static final boolean IS_OPENBSD = OS_NAME_LC.startsWith(OPENBSD);
    public static final boolean IS_LINUX = OS_NAME_LC.startsWith(LINUX);
    public static final boolean IS_SOLARIS = OS_NAME_LC.startsWith(SOLARIS);
    public static final boolean IS_BSD = IS_MAC || IS_FREEBSD || IS_OPENBSD;

    public static String envCommand() {
        if (IS_WINDOWS) {
            if (IS_WINDOWS_9X) {
                return "command.com /c set";
            } else if (IS_WINDOWS_NT || IS_WINDOWS_20X || IS_WINDOWS_XP || IS_WINDOWS_VISTA) {
                return "cmd.exe /c set";
            }
        }
        return "env";
    }

    public static final boolean IS_32_BIT = "32".equals(getProperty("sun.arch.data.model", "32"));
    public static final boolean IS_64_BIT = "64".equals(getProperty("sun.arch.data.model", "64"));

    public static final String ARCH = System.getProperty("os.arch");

    public static final Map<String, String> OS_NAMES = new HashMap<String, String>();

    static {
        OS_NAMES.put("Mac OS X", DARWIN);
        OS_NAMES.put("Darwin", DARWIN);
        OS_NAMES.put("Linux", LINUX);
    }

    public static String getOSName() {
        String theOSName = OS_NAMES.get(OS_NAME);

        return theOSName == null ? OS_NAME : theOSName;
    }

    /**
     * An extension over <code>System.getProperty</code> method.
     * Handles security restrictions, and returns the default
     * value if the access to the property is restricted.
     *
     * @param property The system property name.
     * @param defValue The default value.
     * @return The value of the system property,
     * or the default value.
     */
    public static String getProperty(String property, String defValue) {
        try {
            return System.getProperty(property, defValue);
        } catch (SecurityException se) {
            return defValue;
        }
    }
}
