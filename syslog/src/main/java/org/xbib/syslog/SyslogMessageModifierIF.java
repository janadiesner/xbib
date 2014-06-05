package org.xbib.syslog;

/**
 * SyslogMessageModifierIF provides a common interface for all
 * message modifier implementations.
 */
public interface SyslogMessageModifierIF extends SyslogConstants {
    public String modify(SyslogIF syslog, int facility, int level, String message);

    public boolean verify(String message);
}
