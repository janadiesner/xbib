package org.snmp4j.smi;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The <code>Counter32</code> class allows all the functionality of unsigned
 * integers but is recognized as a distinct SMI type, which is used for
 * monotonically increasing values that wrap around at 2^32-1 (4294967295).
 */
public class Counter32 extends UnsignedInteger32 {

    public Counter32() {
    }

    public Counter32(long value) {
        super(value);
    }

    public boolean equals(Object o) {
        if (o instanceof Counter32) {
            return (((Counter32) o).getValue() == getValue());
        }
        return false;
    }

    public int getSyntax() {
        return SMIConstants.SYNTAX_COUNTER32;
    }

    public void encodeBER(OutputStream outputStream) throws IOException {
        BER.encodeUnsignedInteger(outputStream, BER.COUNTER32, getValue());
    }

    public void decodeBER(BERInputStream inputStream) throws IOException {
        BER.MutableByte type = new BER.MutableByte();
        long newValue = BER.decodeUnsignedInteger(inputStream, type);
        if (type.getValue() != BER.COUNTER32) {
            throw new IOException("Wrong type encountered when decoding Counter: " +
                    type.getValue());
        }
        setValue(newValue);
    }

    public Object clone() {
        return new Counter32(value);
    }

    /**
     * Increment the value of the counter by one. If the current value is
     * 2^32-1 (4294967295) then value will be set to zero.
     */
    public void increment() {
        if (value < 4294967295l) {
            value++;
        } else {
            value = 0;
        }
    }

    public OID toSubIndex(boolean impliedLength) {
        throw new UnsupportedOperationException();
    }

    public void fromSubIndex(OID subIndex, boolean impliedLength) {
        throw new UnsupportedOperationException();
    }

}

