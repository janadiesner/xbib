package org.snmp4j.util;

import org.snmp4j.Session;

/**
 * The <code>AbstractSnmpUtility</code> is an abstract base class for
 * convenience utility classes to retrieve SNMP data.
 */
public abstract class AbstractSnmpUtility {

    protected Session session;
    protected PDUFactory pduFactory;

    /**
     * Creates a <code>AbstractSnmpUtility</code> instance. The created instance
     * is thread safe as long as the supplied <code>Session</code> and
     * <code>PDUFactory</code> are thread safe.
     *
     * @param snmpSession a SNMP <code>Session</code> instance.
     * @param pduFactory  a <code>PDUFactory</code> instance that creates the PDU that are used
     *                    by this instance.
     */
    public AbstractSnmpUtility(Session snmpSession, PDUFactory pduFactory) {
        this.session = snmpSession;
        this.pduFactory = pduFactory;
    }
}
