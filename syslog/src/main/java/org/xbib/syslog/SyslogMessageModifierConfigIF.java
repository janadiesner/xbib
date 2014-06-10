package org.xbib.syslog;

/**
 * SyslogMessageModifierConfigIF provides a common configuration interface for all
 * message modifier implementations.
 */
public interface SyslogMessageModifierConfigIF extends SyslogConstants, SyslogCharSetIF {
    public String getPrefix();

    public void setPrefix(String prefix);

    public String getSuffix();

    public void setSuffix(String suffix);
}
