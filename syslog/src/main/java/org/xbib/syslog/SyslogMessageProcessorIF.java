package org.xbib.syslog;

/**
 * SyslogMessageProcessorIF provides an extensible interface for writing custom
 *  message processors.
 */
public interface SyslogMessageProcessorIF {
    public String createSyslogHeader(int facility, int level, String localName, boolean sendLocalTimestamp, boolean sendLocalName);

    public byte[] createPacketData(byte[] header, byte[] message, int start, int length);

    public byte[] createPacketData(byte[] header, byte[] message, int start, int length, byte[] splitBeginText, byte[] splitEndText);
}
