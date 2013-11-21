
package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PPNTest extends Assert {
    
    @Test
    public void testPPN1() throws Exception {
        PPN ppn = new PPN("641379617");
        String s = ppn.getStandardNumberValue();
        assertEquals(s, "641379617");
    }

    @Test
    public void testPPN2() throws Exception {
        PPN ppn = new PPN("101115658X");
        String s = ppn.getStandardNumberValue();
        assertEquals(s, "101115658X");
    }

}
