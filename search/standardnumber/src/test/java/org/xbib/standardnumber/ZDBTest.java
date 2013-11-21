
package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ZDBTest extends Assert {

    @Test
    public void testZDB1() throws Exception {
        ZDB zdb = new ZDB().setValue("127").parse().verify();
        assertEquals("127", zdb.getValue());
    }

    @Test
    public void testZDB2() throws Exception {
        ZDB zdb = new ZDB().setValue("127976-2").parse().verify();
        assertEquals("1279762", zdb.getValue());
        assertEquals("127976-2", zdb.format());
    }

    @Test
    public void testZDB3() throws Exception {
        ZDB zdb = new ZDB().setValue("1279760").checksum().parse().verify();
        assertEquals("1279762", zdb.getValue());
        assertEquals("127976-2", zdb.format());
    }

}
