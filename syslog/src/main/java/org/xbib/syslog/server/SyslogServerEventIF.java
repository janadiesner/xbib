package org.xbib.syslog.server;

import org.xbib.syslog.SyslogCharSetIF;

import java.util.Date;

/**
 * SyslogServerEventIF provides an extensible interface for
 * server events.
 */
public interface SyslogServerEventIF extends SyslogCharSetIF {

    public int getFacility();

    public void setFacility(int facility);

    public Date getDate();

    public void setDate(Date date);

    public int getLevel();

    public void setLevel(int level);

    public String getHost();

    public void setHost(String host);

    public boolean isHostStrippedFromMessage();

    public String getMessage();

    public void setMessage(String message);
}
