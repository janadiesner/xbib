package org.snmp4j.util;

import org.snmp4j.PDU;
import org.snmp4j.Target;
import org.snmp4j.mp.MessageProcessingModel;

/**
 * <code>PDUFactory</code> defines the interface for PDU factories.
 */
public interface PDUFactory {

    /**
     * Creates a {@link PDU} instance for the supplied target. The created
     * PDU has to be compliant to the SNMP version defined by the supplied target.
     * For example, a SNMPv3 target requires a ScopedPDU instance.
     *
     * @param target the <code>Target</code> where the PDU to be created will be sent.
     * @return PDU
     * a PDU instance that is compatible with the supplied target.
     */
    PDU createPDU(Target target);

    /**
     * Creates a {@link PDU} instance that is compatible with the given SNMP version
     * (message processing model).
     *
     * @param messageProcessingModel a {@link org.snmp4j.mp.MessageProcessingModel} instance.
     * @return a {@link PDU} instance that is compatible with the given SNMP version
     * (message processing model).
     */
    PDU createPDU(MessageProcessingModel messageProcessingModel);

}
