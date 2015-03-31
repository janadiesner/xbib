package org.snmp4j.version;

/**
 * The <code>VersionInfo</code> object returns information about the version
 * of this SNMP4J release.
 */
public class VersionInfo {

    public static final int MAJOR = 2;
    public static final int MINOR = 3;
    public static final int UPDATE = 3;
    public static final String PATCH = "";

    public static final String VERSION =
            MAJOR + "." + MINOR + "." + UPDATE + PATCH;

    private VersionInfo() {
    }

    /**
     * Gets the version string for this release.
     *
     * @return a string of the form <code>major.minor.update[patch]</code>.
     */
    public static String getVersion() {
        return VERSION;
    }

}
