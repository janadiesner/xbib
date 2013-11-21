package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ISMNTest extends Assert {

    @Test
    public void testISMN() throws Exception {
        ISMN ismn = new ISMN().setValue("M-2306-7118-7").parse().verify();
        assertEquals("9790230671187", ismn.getValue());
        assertEquals("9790230671187", ismn.format());
        assertEquals("9790230671187", ismn.checksum().toGTIN().getValue());
    }

    @Test
    public void testISMN2() throws Exception {
        ISMN ismn = new ISMN().setValue("979-0-3452-4680-5").parse().verify();
        assertEquals("9790345246805", ismn.getValue());
        assertEquals("9790345246805", ismn.format());
        assertEquals("9790345246805", ismn.checksum().toGTIN().getValue());
    }

    @Test
    public void testISMNChecksum() throws Exception {
        ISMN ismn = new ISMN().setValue("979-0-3452-4680").checksum().parse().verify();
        assertEquals("9790345246805", ismn.getValue());
        assertEquals("9790345246805", ismn.format());
        assertEquals("9790345246805", ismn.toGTIN().getValue());
    }

}
