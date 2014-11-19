package org.xbib.io.posix;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

public interface POSIXHandler {
    public enum WARNING_ID {
        DUMMY_VALUE_USED("DUMMY_VALUE_USED");

        private String messageID;

        WARNING_ID(String messageID) {
            this.messageID = messageID;
        }
    }

    public void error(POSIX.ERRORS error, String extraData);

    /**
     * Specify that posix method is unimplemented.  In JRuby we generate an
     * exception with this.
     */
    public void unimplementedError(String methodName);

    public void warn(WARNING_ID id, String message, Object... data);

    /**
     * Should we provide verbose output about POSIX activities
     */
    public boolean isVerbose();

    /**
     * Get current working directory of your runtime.
     */
    public File getCurrentWorkingDirectory();

    /**
     * Get current set of environment variables of your runtime.
     */
    public String[] getEnv();

    /**
     * Get your runtime's current InputStream
     */
    public InputStream getInputStream();

    /**
     * Get your runtime's current OutputStream
     */
    public PrintStream getOutputStream();

    /**
     * Get your runtimes process ID.  This is only intended for non-native POSIX support (e.g.
     * environments where JNA cannot load or security restricted environments).  In JRuby we
     * found a number of packages which would rather have some identity for the runtime than
     * nothing.
     * <p>
     * Note: If you do not want this to work you impl can just call unimplementedError(String).
     */
    public int getPID();

    /**
     * Get your runtime's current ErrorStream
     */
    public PrintStream getErrorStream();
}
