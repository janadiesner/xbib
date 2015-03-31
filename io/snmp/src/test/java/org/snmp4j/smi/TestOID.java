package org.snmp4j.smi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.mp.SnmpConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestOID extends Assert {

    private final static Logger logger = LogManager.getLogger(TestOID.class);

    private static void printOIDs(OID oid) {
        if (logger.isDebugEnabled()) {
            logger.debug("OID=" + oid + ", predecessor=" + oid.predecessor() +
                    ",successor=" + oid.successor());
        }
    }

    @Test
    public void testCompareTo() {
        OID oID = new OID(SnmpConstants.usmStatsUnknownEngineIDs);

        OID o = SnmpConstants.usmStatsNotInTimeWindows;
        int expectedReturn = 1;
        int actualReturn = oID.compareTo(o);
        assertEquals(expectedReturn, actualReturn);
        o = SnmpConstants.usmStatsUnknownEngineIDs;
        expectedReturn = 0;
        actualReturn = oID.compareTo(o);
        assertEquals(expectedReturn, actualReturn);
        o = SnmpConstants.usmStatsWrongDigests;
        expectedReturn = -1;
        actualReturn = oID.compareTo(o);
        assertEquals(expectedReturn, actualReturn);

        OID a = new OID(new int[]{1, 2, 3, 6, 0x80000000});
        OID b = new OID(new int[]{1, 2, 3, 6, 0x80000001});
        expectedReturn = 1;
        actualReturn = b.compareTo(a);
        assertEquals(expectedReturn, actualReturn);

        expectedReturn = -1;
        actualReturn = a.compareTo(b);
        assertEquals(expectedReturn, actualReturn);
    }

    @Test
    public void testLeftMostCompare() {
        OID oID = new OID(SnmpConstants.usmStatsUnknownEngineIDs);

        OID other = SnmpConstants.snmpInASNParseErrs;
        int n = Math.min(other.size(), oID.size());
        int expectedReturn = 1;
        int actualReturn = oID.leftMostCompare(n, other);
        assertEquals(expectedReturn, actualReturn);
    }

    @Test
    public void testRightMostCompare() {
        OID oID = new OID(SnmpConstants.usmStatsUnknownEngineIDs);
        int n = 2;
        OID other = SnmpConstants.usmStatsUnsupportedSecLevels;
        int expectedReturn = 1;
        int actualReturn = oID.rightMostCompare(n, other);
        assertEquals(expectedReturn, actualReturn);
    }

    @Test
    public void testPredecessor() {
        OID oid = new OID("1.3.6.4.1.5");
        printOIDs(oid);
        assertEquals(oid.predecessor().successor(), oid);
        oid = new OID("1.3.6.4.1.5.0");
        printOIDs(oid);
        assertEquals(oid.predecessor().successor(), oid);
        oid = new OID("1.3.6.4.1.5.2147483647");
        printOIDs(oid);
        assertEquals(oid.predecessor().successor(), oid);
    }

    @Test
    public void testStartsWith() {
        OID oID = new OID(SnmpConstants.usmStatsUnknownEngineIDs);

        OID other = new OID(SnmpConstants.usmStatsDecryptionErrors.getValue());
        other.removeLast();
        other.removeLast();
        boolean actualReturn = oID.startsWith(other);
        assertEquals(true, actualReturn);

        other = new OID(SnmpConstants.usmStatsUnknownEngineIDs.getValue());
        actualReturn = oID.startsWith(other);
        assertEquals(true, actualReturn);

        other = new OID(SnmpConstants.usmStatsUnknownEngineIDs.getValue());
        other.append("33.44");
        actualReturn = oID.startsWith(other);
        assertEquals(false, actualReturn);
    }

    @Test
    public void testStringParse() {
        OID a = new OID("1.3.6.2.1.5.'hallo'.1");
        OID b = new OID("1.3.6.2.1.5.104.97.108.108.111.1");
        assertEquals(a, b);
        a = new OID("1.3.6.2.1.5.'hal.lo'.1");
        b = new OID("1.3.6.2.1.5.104.97.108.46.108.111.1");
        assertEquals(a, b);
        a = new OID("1.3.6.2.1.5.'hal.'.'''.'lo'.1");
        b = new OID("1.3.6.2.1.5.104.97.108.46.39.108.111.1");
        assertEquals(a, b);
    }

}
