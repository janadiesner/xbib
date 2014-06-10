package org.xbib.syslog.impl.message.modifier.checksum;

import org.xbib.syslog.impl.message.modifier.AbstractSyslogMessageModifierConfig;

import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * ChecksumSyslogMessageModifierConfig is an implementation of AbstractSyslogMessageModifierConfig
 * that provides configuration for ChecksumSyslogMessageModifier.
 */
public class ChecksumSyslogMessageModifierConfig extends AbstractSyslogMessageModifierConfig {

    protected Checksum checksum = null;
    protected boolean continuous = false;

    public static final ChecksumSyslogMessageModifierConfig createCRC32() {
        ChecksumSyslogMessageModifierConfig crc32 = new ChecksumSyslogMessageModifierConfig(new CRC32());

        return crc32;
    }

    public static final ChecksumSyslogMessageModifierConfig createADLER32() {
        ChecksumSyslogMessageModifierConfig adler32 = new ChecksumSyslogMessageModifierConfig(new Adler32());

        return adler32;
    }

    public ChecksumSyslogMessageModifierConfig(Checksum checksum) {
        this.checksum = checksum;
    }

    public Checksum getChecksum() {
        return this.checksum;
    }

    public void setChecksum(Checksum checksum) {
        this.checksum = checksum;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }
}
