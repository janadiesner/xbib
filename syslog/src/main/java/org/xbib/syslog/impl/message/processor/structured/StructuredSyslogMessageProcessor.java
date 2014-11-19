package org.xbib.syslog.impl.message.processor.structured;

import org.xbib.syslog.impl.message.processor.AbstractSyslogMessageProcessor;
import org.xbib.syslog.impl.message.structured.StructuredSyslogMessage;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
//import org.xbib.time.Instant;
//import org.xbib.time.format.DateTimeFormatter;

/**
 * SyslogStructuredMessageProcessor extends SyslogMessageProcessor's ability to
 * split a syslog message into multiple messages when the message is greater
 * than the syslog maximum message length (1024 bytes including the header). It
 * adds support for structured syslog messages as specified by
 * draft-ietf-syslog-protocol-23. More information here:
 * <p>
 * <p>http://tools.ietf.org/html/draft-ietf-syslog-protocol-23</p>
 * <p>
 * <p>Those wishing to replace (or improve upon) this implementation
 * can write a custom SyslogMessageProcessorIF and set it per
 * instance via the SyslogIF.setStructuredMessageProcessor(..) method or set it globally
 * via the StructuredSyslogMessageProcessor.setDefault(..) method.</p>
 */
public class StructuredSyslogMessageProcessor extends AbstractSyslogMessageProcessor {

    public static String VERSION = "1";

    private static final StructuredSyslogMessageProcessor INSTANCE = new StructuredSyslogMessageProcessor();
    protected static StructuredSyslogMessageProcessor defaultInstance = INSTANCE;

    private String applicationName = STRUCTURED_DATA_APP_NAME_DEFAULT_VALUE;
    private String processId = STRUCTURED_DATA_PROCESS_ID_DEFAULT_VALUE;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

    public static void setDefault(StructuredSyslogMessageProcessor messageProcessor) {
        if (messageProcessor != null) {
            defaultInstance = messageProcessor;
        }
    }

    public static StructuredSyslogMessageProcessor getDefault() {
        return defaultInstance;
    }

    public StructuredSyslogMessageProcessor() {
        super();
    }

    public StructuredSyslogMessageProcessor(final String applicationName) {
        super();
        this.applicationName = applicationName;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getProcessId() {
        return this.processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String createSyslogHeader(final int facility, final int level, String localName, final boolean sendLocalTimestamp, final boolean sendLocalName) {
        final StringBuffer buffer = new StringBuffer();

        appendPriority(buffer, facility, level);
        buffer.append(VERSION);
        buffer.append(' ');

        getDateTimeFormatter().format(Instant.now());
        buffer.append(' ');

        appendLocalName(buffer, localName);

        buffer.append(StructuredSyslogMessage.nilProtect(this.applicationName))
                .append(' ');

        buffer.append(StructuredSyslogMessage.nilProtect(this.processId)).append(' ');

        return buffer.toString();
    }
}
