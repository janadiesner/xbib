package org.snmp4j.smi;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The <code>Opaque</code> class represents the SMI type Opaque which is used
 * to transparently exchange BER encoded values.
 */
public class Opaque extends OctetString {

    public Opaque() {
        super();
    }

    public Opaque(byte[] bytes) {
        super(bytes);
    }

    public int getSyntax() {
        return SMIConstants.SYNTAX_OPAQUE;
    }

    public void encodeBER(OutputStream outputStream) throws IOException {
        BER.encodeString(outputStream, BER.OPAQUE, getValue());
    }

    public void decodeBER(BERInputStream inputStream) throws IOException {
        BER.MutableByte type = new BER.MutableByte();
        byte[] v = BER.decodeString(inputStream, type);
        if (type.getValue() != (BER.ASN_APPLICATION | 0x04)) {
            throw new IOException("Wrong type encountered when decoding OctetString: " +
                    type.getValue());
        }
        setValue(v);
    }

    public void setValue(OctetString value) {
        this.setValue(new byte[0]);
        append(value);
    }

    public String toString() {
        return super.toHexString();
    }

    public Object clone() {
        return new Opaque(super.getValue());
    }

}

