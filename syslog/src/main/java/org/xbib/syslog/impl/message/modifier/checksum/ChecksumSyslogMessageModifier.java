package org.xbib.syslog.impl.message.modifier.checksum;

import org.xbib.syslog.Syslogger;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.message.modifier.AbstractSyslogMessageModifier;
import org.xbib.syslog.util.SyslogUtility;

/**
 * ChecksumSyslogMessageModifier is an implementation of SyslogMessageModifierIF
 * that provides support for Java Checksum algorithms (java.util.zip.Checksum).
 */
public class ChecksumSyslogMessageModifier extends AbstractSyslogMessageModifier {

    protected ChecksumSyslogMessageModifierConfig config = null;

    public static ChecksumSyslogMessageModifier createCRC32() {
        return new ChecksumSyslogMessageModifier(ChecksumSyslogMessageModifierConfig.createCRC32());
    }

    public static ChecksumSyslogMessageModifier createADLER32() {
        return new ChecksumSyslogMessageModifier(ChecksumSyslogMessageModifierConfig.createADLER32());
    }

    public ChecksumSyslogMessageModifier(ChecksumSyslogMessageModifierConfig config) {
        super(config);

        this.config = config;

        if (this.config == null) {
            throw new SyslogRuntimeException("Checksum config object cannot be null");
        }

        if (this.config.getChecksum() == null) {
            throw new SyslogRuntimeException("Checksum object cannot be null");
        }
    }

    public ChecksumSyslogMessageModifierConfig getConfig() {
        return this.config;
    }

    protected void continuousCheckForVerify() {
        if (this.config.isContinuous()) {
            throw new SyslogRuntimeException(this.getClass().getName() + ".verify(..) does not work with isContinuous() returning true");
        }

    }

    public boolean verify(String message, String hexChecksum) {
        continuousCheckForVerify();

        long checksum = Long.parseLong(hexChecksum, 16);

        return verify(message, checksum);
    }

    public boolean verify(String message, long checksum) {
        continuousCheckForVerify();

        synchronized (this.config.getChecksum()) {
            this.config.getChecksum().reset();

            byte[] messageBytes = SyslogUtility.getBytes(this.config, message);

            this.config.getChecksum().update(messageBytes, 0, message.length());

            return this.config.getChecksum().getValue() == checksum;
        }
    }

    public String modify(Syslogger syslog, int facility, int level, String message) {
        synchronized (this.config.getChecksum()) {
            StringBuilder messageBuffer = new StringBuilder(message);
            byte[] messageBytes = SyslogUtility.getBytes(syslog.getConfig(), message);
            if (!this.config.isContinuous()) {
                this.config.getChecksum().reset();
            }
            this.config.getChecksum().update(messageBytes, 0, message.length());
            messageBuffer.append(this.config.getPrefix());
            messageBuffer.append(Long.toHexString(this.config.getChecksum().getValue()).toUpperCase());
            messageBuffer.append(this.config.getSuffix());
            return messageBuffer.toString();
        }
    }
}
