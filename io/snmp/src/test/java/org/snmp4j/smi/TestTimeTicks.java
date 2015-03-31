package org.snmp4j.smi;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTimeTicks extends Assert {

    @Test
    public void testToString() {
        TimeTicks timeticks = new TimeTicks();
        String stringRet = timeticks.toString();
        assertEquals("0:00:00.00", stringRet);
    }

    @Test
    public void testToMaxValue() {
        TimeTicks timeticks = new TimeTicks(4294967295L);
        String stringRet = timeticks.toString();
        System.out.println(stringRet);
        assertEquals("497 days, 2:27:52.95", stringRet);
    }

}
