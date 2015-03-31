package org.snmp4j.mp;

/**
 * The <code>PduHandleCallback</code> can be used to get informed about a
 * <code>PduHandle</code> creation before a request is actually sent out.
 */
public interface PduHandleCallback<P> {

    /**
     * A new PduHandle has been created for a PDU. This event callback
     * notification can be used to get informed about a new PduHandle
     * (just) before a PDU has been sent out.
     *
     * @param handle a <code>PduHandle</code> instance that uniquely identifies a request -
     *               thus in most cases the request ID.
     * @param pdu    the request PDU for which the handle has been created.
     */
    void pduHandleAssigned(PduHandle handle, P pdu);

}
