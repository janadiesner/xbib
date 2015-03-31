package org.snmp4j;

/**
 * The <code>MutablePDU</code> is a container for a <code>PDU</code>
 * instance.
 */
public class MutablePDU {

    private PDU pdu;

    public MutablePDU() {
    }

    public PDU getPdu() {
        return pdu;
    }

    public void setPdu(PDU pdu) {
        this.pdu = pdu;
    }
}
