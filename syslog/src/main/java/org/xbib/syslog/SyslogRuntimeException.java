package org.xbib.syslog;

/**
 * SyslogRuntimeException provides an extension of RuntimeException thrown
 * by the majority of the classes
 */
public class SyslogRuntimeException extends RuntimeException {

    public SyslogRuntimeException(String arg0) {
        super(arg0);
    }

    public SyslogRuntimeException(Throwable arg0) {
        super(arg0);
    }
}
