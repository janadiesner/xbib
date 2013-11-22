
package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UPCTest extends Assert {

    @Test
    public void testUPC() throws Exception {
        String value = "796030114977";
        UPC upc = new UPC().set(value).normalize().verify();
        assertEquals("796030114977", upc.normalized());
        assertEquals("796030114977", upc.format());
    }

    @Test
    public void testUPC2() throws Exception {
        String value = "036000291452";
        UPC upc = new UPC().set(value).normalize().verify();
        assertEquals("036000291452", upc.normalized());
        assertEquals("036000291452", upc.format());
    }

}
