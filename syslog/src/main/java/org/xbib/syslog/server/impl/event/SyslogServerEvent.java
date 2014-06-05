package org.xbib.syslog.server.impl.event;

import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.server.SyslogServerEventIF;
import org.xbib.syslog.util.SyslogUtility;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * SyslogServerEvent provides an implementation of the SyslogServerEventIF interface.
 */
public class SyslogServerEvent implements SyslogServerEventIF {

    public static final String DATE_FORMAT = "MMM dd HH:mm:ss yyyy";

    protected String charSet = SyslogConstants.CHAR_SET_DEFAULT;
    protected String rawString = null;
    protected byte[] rawBytes = null;
    protected int rawLength = -1;
    protected Date date = null;
    protected int level = -1;
    protected int facility = -1;
    protected String host = null;
    protected boolean isHostStrippedFromMessage = false;
    protected String message = null;
    protected InetAddress inetAddress = null;

    protected SyslogServerEvent() {
    }

    public SyslogServerEvent(final String message, InetAddress inetAddress) {
        initialize(message, inetAddress);

        parse();
    }

    public SyslogServerEvent(final byte[] message, int length, InetAddress inetAddress) {
        initialize(message, length, inetAddress);

        parse();
    }

    protected void initialize(final String message, InetAddress inetAddress) {
        this.rawString = message;
        this.rawLength = message.length();
        this.inetAddress = inetAddress;

        this.message = message;
    }

    protected void initialize(final byte[] message, int length, InetAddress inetAddress) {
        this.rawBytes = message;
        this.rawLength = length;
        this.inetAddress = inetAddress;
    }

    protected void parseHost() {
        int i = this.message.indexOf(' ');

        if (i > -1) {
            String hostAddress = null;
            String hostName = null;

            String providedHost = this.message.substring(0, i).trim();

            hostAddress = this.inetAddress.getHostAddress();

            if (providedHost.equalsIgnoreCase(hostAddress)) {
                this.host = hostAddress;
                this.message = this.message.substring(i + 1);
                isHostStrippedFromMessage = true;
            }

            if (this.host == null) {
                hostName = this.inetAddress.getHostName();

                if (!hostName.equalsIgnoreCase(hostAddress)) {
                    if (providedHost.equalsIgnoreCase(hostName)) {
                        this.host = hostName;
                        this.message = this.message.substring(i + 1);
                        isHostStrippedFromMessage = true;
                    }

                    if (this.host == null) {
                        int j = hostName.indexOf('.');

                        if (j > -1) {
                            hostName = hostName.substring(0, j);
                        }

                        if (providedHost.equalsIgnoreCase(hostName)) {
                            this.host = hostName;
                            this.message = this.message.substring(i + 1);
                            isHostStrippedFromMessage = true;
                        }
                    }
                }
            }

            if (this.host == null) {
                this.host = (hostName != null) ? hostName : hostAddress;
            }
        }
    }

    protected void parseDate() {
        if (this.message.length() >= 16 && this.message.charAt(3) == ' ' && this.message.charAt(6) == ' ') {
            String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));

            String originalDate = this.message.substring(0, 15) + " " + year;

            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                this.date = dateFormat.parse(originalDate);

                this.message = this.message.substring(16);

            } catch (ParseException pe) {
                this.date = new Date();
            }
        }

        parseHost();
    }

    protected void parsePriority() {
        if (this.message.charAt(0) == '<') {
            int i = this.message.indexOf(">");

            if (i <= 4 && i > -1) {
                String priorityStr = this.message.substring(1, i);

                int priority = 0;
                try {
                    priority = Integer.parseInt(priorityStr);
                    this.facility = priority >> 3;
                    this.level = priority - (this.facility << 3);

                    this.message = this.message.substring(i + 1);

                    parseDate();

                } catch (NumberFormatException nfe) {
                    //
                }

                parseHost();
            }
        }
    }

    protected void parse() {
        if (this.message == null) {
            this.message = SyslogUtility.newString(this, this.rawBytes, this.rawLength);
        }

        parsePriority();
    }

    public int getFacility() {
        return this.facility;
    }

    public void setFacility(int facility) {
        this.facility = facility;
    }

    public byte[] getRaw() {
        if (this.rawString != null) {
            byte[] rawStringBytes = SyslogUtility.getBytes(this, this.rawString);

            return rawStringBytes;

        } else if (this.rawBytes.length == this.rawLength) {
            return this.rawBytes;

        } else {
            byte[] newRawBytes = new byte[this.rawLength];
            System.arraycopy(this.rawBytes, 0, newRawBytes, 0, this.rawLength);

            return newRawBytes;
        }
    }

    public int getRawLength() {
        return this.rawLength;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isHostStrippedFromMessage() {
        return isHostStrippedFromMessage;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCharSet() {
        return this.charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }
}
