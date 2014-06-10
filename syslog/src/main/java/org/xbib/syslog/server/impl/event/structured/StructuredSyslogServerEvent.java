package org.xbib.syslog.server.impl.event.structured;

import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.impl.message.structured.StructuredSyslogMessage;
import org.xbib.syslog.server.impl.event.SyslogServerEvent;
import org.xbib.time.Instant;
import org.xbib.time.LocalDateTime;
import org.xbib.time.ZoneId;
import org.xbib.time.format.DateTimeFormatter;

import java.net.InetAddress;
import java.util.Date;

/**
 * SyslogServerStructuredEvent provides an implementation of the
 * SyslogServerEventIF interface that supports receiving of structured syslog
 * messages, as defined in:
 * http://tools.ietf.org/html/draft-ietf-syslog-protocol-23#section-6
 */
public class StructuredSyslogServerEvent extends SyslogServerEvent {

    protected String applicationName = SyslogConstants.STRUCTURED_DATA_APP_NAME_DEFAULT_VALUE;

    protected String processId = null;

    protected LocalDateTime dateTime = null;

    protected DateTimeFormatter dateTimeFormatter = null;

    public StructuredSyslogServerEvent(final byte[] message, int length, InetAddress inetAddress) {
        super();

        initialize(message, length, inetAddress);
        parse();
    }

    public StructuredSyslogServerEvent(final String message, InetAddress inetAddress) {
        super();

        initialize(message, inetAddress);
        parse();
    }

    public DateTimeFormatter getDateTimeFormatter() {
        if (dateTimeFormatter == null) {
            this.dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        }

        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(Object dateTimeFormatter) {
        this.dateTimeFormatter = (DateTimeFormatter) dateTimeFormatter;
    }

    protected void parseApplicationName() {
        int i = this.message.indexOf(' ');

        if (i > -1) {
            this.applicationName = this.message.substring(0, i).trim();
            this.message = this.message.substring(i + 1);
            parseProcessId();
        }

        if (SyslogConstants.STRUCTURED_DATA_NILVALUE.equals(this.applicationName)) {
            this.applicationName = null;
        }
    }

    protected void parseProcessId() {
        int i = this.message.indexOf(' ');

        if (i > -1) {
            this.processId = this.message.substring(0, i).trim();
            this.message = this.message.substring(i + 1);
        }

        if (SyslogConstants.STRUCTURED_DATA_NILVALUE.equals(this.processId)) {
            this.processId = null;
        }
    }

    protected void parseDate() {
        // skip VERSION field
        int i = this.message.indexOf(' ');
        this.message = this.message.substring(i + 1);

        // parse the date
        i = this.message.indexOf(' ');

        if (i > -1) {
            String dateString = this.message.substring(0, i).trim();

            try {
                Instant instant = Instant.parse(dateString);
                this.dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
                this.date = new Date(instant.toEpochMilli());

                this.message = this.message.substring(dateString.length() + 1);

            } catch (Exception e) {
                // Not structured date format, try super one
                super.parseDate();
            }
        }
    }

    protected void parseHost() {
        int i = this.message.indexOf(' ');

        if (i > -1) {
            this.host = this.message.substring(0, i).trim();
            this.message = this.message.substring(i + 1);

            parseApplicationName();
        }
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public String getProcessId() {
        return this.processId;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public StructuredSyslogMessage getStructuredMessage() {
        try {
            return StructuredSyslogMessage.fromString(getMessage());

        } catch (IllegalArgumentException e) {
            // throw new SyslogRuntimeException(
            // "Message received is not a valid structured message: "
            // + getMessage(), e);
            return new StructuredSyslogMessage(null, null, getMessage());
        }
    }
}
