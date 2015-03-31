package org.snmp4j;

import org.snmp4j.mp.PduHandle;
import org.snmp4j.mp.StateReference;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;

import java.util.EventObject;

/**
 * The <code>CommandResponderEvent</code> is fired by the
 * <code>MessageDispatcher</code> to listeners that potentially can process
 * the included request, report, or trap/notification.
 *
 */
public class CommandResponderEvent extends EventObject {

    private int securityModel;
    private int securityLevel;
    private int maxSizeResponsePDU;
    private PduHandle pduHandle;
    private StateReference stateReference;
    private PDU pdu;
    private int messageProcessingModel;
    private byte[] securityName;
    private boolean processed;
    private Address peerAddress;
    private transient TransportMapping transportMapping;
    private TransportStateReference tmStateReference;

    /**
     * Constructs an event for processing an incoming request or notification PDU.
     *
     * @param messageDispatcher        the source of the event. May be used to send response PDUs.
     * @param transportMapping         the <code>TransportMapping</code> which received the PDU.
     * @param sourceAddress            the source transport address of the SNMP message.
     * @param messageProcessingModel   the message processing model ID.
     * @param securityModel            the security model ID.
     * @param securityName             the principal.
     * @param securityLevel            the requested security level.
     * @param pduHandle                the PDU handle that uniquely identifies the <code>pdu</code>.
     * @param pdu                      the SNMP request PDU to process.
     * @param maxSizeResponseScopedPDU the maximum size of a possible response PDU.
     * @param stateReference           needed for responding a request, will be <code>null</code> for
     *                                 notifications.
     */
    public CommandResponderEvent(MessageDispatcher messageDispatcher,
                                 TransportMapping transportMapping,
                                 Address sourceAddress,
                                 int messageProcessingModel,
                                 int securityModel,
                                 byte[] securityName,
                                 int securityLevel,
                                 PduHandle pduHandle,
                                 PDU pdu,
                                 int maxSizeResponseScopedPDU,
                                 StateReference stateReference) {
        super(messageDispatcher);
        setTransportMapping(transportMapping);
        setMessageProcessingModel(messageProcessingModel);
        setSecurityModel(securityModel);
        setSecurityName(securityName);
        setSecurityLevel(securityLevel);
        setPduHandle(pduHandle);
        setPDU(pdu);
        setMaxSizeResponsePDU(maxSizeResponseScopedPDU);
        setStateReference(stateReference);
        setPeerAddress(sourceAddress);
    }

    /**
     * Creates shallow copy of the supplied <code>CommandResponderEvent</code>
     * but the source of the event is set to the supplied source.
     *
     * @param source the (new) source of event copy to create.
     * @param other  the <code>CommandResponderEvent</code> to copy.
     */
    public CommandResponderEvent(Object source, CommandResponderEvent other) {
        super(source);
        setTransportMapping(other.transportMapping);
        setMessageProcessingModel(other.messageProcessingModel);
        setSecurityModel(other.securityModel);
        setSecurityName(other.securityName);
        setSecurityLevel(other.securityLevel);
        setPduHandle(other.pduHandle);
        setPDU(other.pdu);
        setMaxSizeResponsePDU(other.maxSizeResponsePDU);
        setStateReference(other.stateReference);
        setPeerAddress(other.getPeerAddress());
    }

    /**
     * Gets the message dispatcher instance that received the command
     * (request PDU) or unconfirmed PDU like a report, trap, or notification..
     *
     * @return the <code>MessageDispatcher</code> instance that received the command.
     */
    public MessageDispatcher getMessageDispatcher() {
        return (MessageDispatcher) super.getSource();
    }

    /**
     * Gets the security model used by the command.
     *
     * @return int
     */
    public int getSecurityModel() {
        return securityModel;
    }

    public void setSecurityModel(int securityModel) {
        this.securityModel = securityModel;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public int getMaxSizeResponsePDU() {
        return maxSizeResponsePDU;
    }

    public void setMaxSizeResponsePDU(int maxSizeResponsePDU) {
        this.maxSizeResponsePDU = maxSizeResponsePDU;
    }

    public PduHandle getPduHandle() {
        return pduHandle;
    }

    public void setPduHandle(PduHandle pduHandle) {
        this.pduHandle = pduHandle;
    }

    public StateReference getStateReference() {
        return stateReference;
    }

    public void setStateReference(StateReference stateReference) {
        this.stateReference = stateReference;
    }

    public PDU getPDU() {
        return pdu;
    }

    public void setPDU(PDU pdu) {
        this.pdu = pdu;
    }

    public int getMessageProcessingModel() {
        return messageProcessingModel;
    }

    public void setMessageProcessingModel(int messageProcessingModel) {
        this.messageProcessingModel = messageProcessingModel;
    }

    public byte[] getSecurityName() {
        return securityName;
    }

    public void setSecurityName(byte[] securityName) {
        this.securityName = securityName;
    }

    /**
     * Checks whether this event is already processed or not.
     *
     * @return <code>true</code> if this event has been processed, <code>false</code>
     * otherwise.
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Sets the status of this PDU.
     *
     * @param processed If set to <code>true</code>, the dispatcher stops dispatching this
     *                  event to other event listeners, because it has been successfully
     *                  processed.
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /**
     * Gets the transport address of the sending entity.
     *
     * @return the <code>Address</code> of the PDU sender.
     */
    public Address getPeerAddress() {
        return peerAddress;
    }

    /**
     * Sets the transport address of the sending entity.
     *
     * @param peerAddress the <code>Address</code> of the PDU sender.
     */
    public void setPeerAddress(Address peerAddress) {
        this.peerAddress = peerAddress;
    }

    /**
     * Returns the transport mapping that received the PDU that triggered this
     * event.
     *
     * @return a <code>TransportMapping</code> instance.
     */
    public TransportMapping getTransportMapping() {
        return transportMapping;
    }

    protected void setTransportMapping(TransportMapping transportMapping) {
        this.transportMapping = transportMapping;
    }

    /**
     * Gets the transport model state reference as defined by RFC 5590.
     *
     * @return a {@link TransportStateReference} instance if the transport and/or
     * the security model supports it or <code>null</code> otherwise.
     */
    public TransportStateReference getTmStateReference() {
        return tmStateReference;
    }

    /**
     * Sets the transport model state reference as defined by RFC 5590.
     *
     * @param tmStateReference the transport model (mapping) state information associated with
     *                         this command responder event.
     */
    public void setTmStateReference(TransportStateReference tmStateReference) {
        this.tmStateReference = tmStateReference;
    }

    @Override
    public String toString() {
        return "CommandResponderEvent[" +
                "securityModel=" + securityModel +
                ", securityLevel=" + securityLevel +
                ", maxSizeResponsePDU=" + maxSizeResponsePDU +
                ", pduHandle=" + pduHandle +
                ", stateReference=" + stateReference +
                ", pdu=" + pdu +
                ", messageProcessingModel=" + messageProcessingModel +
                ", securityName=" + new OctetString(securityName) +
                ", processed=" + processed +
                ", peerAddress=" + peerAddress +
                ", transportMapping=" + transportMapping +
                ", tmStateReference=" + tmStateReference +
                ']';
    }
}

