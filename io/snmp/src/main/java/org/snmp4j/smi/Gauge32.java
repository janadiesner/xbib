package org.snmp4j.smi;

/**
 * The <code>Gauge32</code> class is indistinguishable from
 * <code>UnsingedInteger32</code>.
 */
public class Gauge32 extends UnsignedInteger32 {

    public Gauge32() {
    }

    public Gauge32(long value) {
        super(value);
    }

    public int getSyntax() {
        return SMIConstants.SYNTAX_GAUGE32;
    }

    public Object clone() {
        return new Gauge32(value);
    }

}

