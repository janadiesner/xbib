
package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ZDBTest extends Assert {

    @Test
    public void testZDB1() throws Exception {
        ZDB zdb = new ZDB("127");
        String s = zdb.getStandardNumberValue();
        assertEquals(s, "127");
    }

    @Test
    public void testZDB2() throws Exception {
        ZDB zdb = new ZDB("127976-2");
        String s = zdb.getStandardNumberValue();
        assertEquals(s, "1279762");
    }


}
