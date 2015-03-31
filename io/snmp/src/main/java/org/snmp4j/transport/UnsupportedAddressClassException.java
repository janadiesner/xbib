package org.snmp4j.transport;

import org.snmp4j.MessageException;

/**
 * The <code>UnsupportedAddressClassException</code> indicates a message
 * exception caused by unsupported address class. When this exception is
 * thrown, the target address class is not supported by the entity that
 * is to sent the message and that operation will be canceled.
 */
public class UnsupportedAddressClassException extends MessageException {

    private Class addressClass;

    public UnsupportedAddressClassException(String message, Class addressClass) {
        super(message);
        this.addressClass = addressClass;
    }

    /**
     * Returns the class of the address class that is not supported.
     *
     * @return a Class.
     */
    public Class getAddressClass() {
        return addressClass;
    }
}
