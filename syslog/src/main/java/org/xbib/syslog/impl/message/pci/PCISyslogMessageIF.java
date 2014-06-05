package org.xbib.syslog.impl.message.pci;

/**
 * PCISyslogMessageIF provides a definition of the fields for audit trails
 * defined by section 10.3 of the PCI Data Security Standard (PCI DSS)
 * versions 1.1 and 1.2.
 * <p>
 * <p>More information on the PCI DSS specification is available here:</p>
 * <p>
 * <p>https://www.pcisecuritystandards.org/security_standards/pci_dss.shtml</p>
 * <p>
 * <p>The PCI DSS specification is Copyright 2008 PCI Security Standards
 * Council LLC.</p>
 */
public interface PCISyslogMessageIF {
    public String getUserId();

    public String getEventType();

    public String getDate();

    public String getTime();

    public String getStatus();

    public String getOrigination();

    public String getAffectedResource();
}
