package org.xbib.syslog;

/**
 * SyslogCharSetIF provides control of the encoding character set
 */
public interface SyslogCharSetIF {
    String getCharSet();

    void setCharSet(String charSet);
}
