package org.xbib.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ILLNumberTest extends Assert {

    @Test
    public void testILLNumber() throws Exception {
        ILL ill = new ILL().set("DE-605-2012-301-0-5");
        ill.normalize().verify();
        String s = ill.normalizedValue();
        assertEquals(s,"DE-605-2012-301-0-5");
        s = ill.format();
        assertEquals(s,"DE-605-2012-301-0-5");
    }

    @Test
    public void testILLNumber2() throws Exception {
        ILL ill = new ILL().set("DE-605-2012-2324580-0-8");
        ill.normalize().verify();
        String s = ill.normalizedValue();
        assertEquals(s,"DE-605-2012-2324580-0-8");
        s = ill.format();
        assertEquals(s,"DE-605-2012-2324580-0-8");
    }

}
