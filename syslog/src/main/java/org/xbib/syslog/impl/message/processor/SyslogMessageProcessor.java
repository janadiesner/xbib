package org.xbib.syslog.impl.message.processor;


/**
 * SyslogMessageProcessor wraps AbstractSyslogMessageProcessor.
 * <p>
 * <p>Those wishing to replace (or improve upon) this implementation
 * can write a custom SyslogMessageProcessorIF and set it per
 * instance via the SyslogIF.setMessageProcessor(..) method or set it globally
 * via the SyslogMessageProcessor.setDefault(..) method.</p>
 */
public class SyslogMessageProcessor extends AbstractSyslogMessageProcessor {

    private static final SyslogMessageProcessor INSTANCE = new SyslogMessageProcessor();

    protected static SyslogMessageProcessor defaultInstance = INSTANCE;

    public static void setDefault(SyslogMessageProcessor messageProcessor) {
        if (messageProcessor != null) {
            defaultInstance = messageProcessor;
        }
    }

    public static SyslogMessageProcessor getDefault() {
        return defaultInstance;
    }
}
