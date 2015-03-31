package org.snmp4j.smi;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.StringTokenizer;

public class OctetStringTest extends Assert {

    @Test
    public void testConstructors() {
        byte[] ba = {
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};

        OctetString octetString = new OctetString(ba);

        assertEquals(octetString.toString(), "abcdefghi");

        octetString = new OctetString(ba, 2, 2);
        assertEquals(octetString.toString(), "cd");
    }

    @Test
    public void testSlip() {
        String s = "A short string with several delimiters  and a short word!";
        OctetString sp = new OctetString(s);
        Collection<OctetString> words = OctetString.split(sp, new OctetString("! "));
        StringTokenizer st = new StringTokenizer(s, "! ");
        for (OctetString os : words) {
            assertEquals(os.toString(), st.nextToken());
        }
        assertFalse(st.hasMoreTokens());
    }

    public void testIsPrintable() {
        OctetString nonPrintable = OctetString.fromHexString("1C:32:41:1C:4E:38");
        assertFalse(nonPrintable.isPrintable());
    }
}
