
package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PPNTest extends Assert {
    
    @Test
    public void testPPN1() throws Exception {
        PPN ppn = new PPN().setValue("641379617").parse().verify();
        assertEquals(ppn.getValue(), "641379617");
    }

    @Test
    public void testPPN2() throws Exception {
        PPN ppn = new PPN().setValue("101115658X").parse().verify();
        assertEquals(ppn.getValue(), "101115658X");
    }

}
