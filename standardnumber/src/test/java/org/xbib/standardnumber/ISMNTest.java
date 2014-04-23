package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ISMNTest extends Assert {

    @Test
    public void testISMN() throws Exception {
        ISMN ismn = new ISMN().set("M-2306-7118-7").normalize().verify();
        assertEquals("9790230671187", ismn.normalizedValue());
        assertEquals("979-0-2306-7118-7", ismn.format());
        assertEquals("9790230671187", ismn.checksum().toGTIN().normalizedValue());
    }

    @Test
    public void testISMN2() throws Exception {
        ISMN ismn = new ISMN().set("979-0-3452-4680-5").normalize().verify();
        assertEquals("9790345246805", ismn.normalizedValue());
        assertEquals("979-0-345246805", ismn.format());
        assertEquals("9790345246805", ismn.checksum().toGTIN().normalizedValue());
    }

    @Test
    public void testISMN3() throws Exception {
        ISMN ismn = new ISMN().set("979-0-2042-2521-7").normalize().verify();
        assertEquals("9790204225217", ismn.normalizedValue());
        assertEquals("979-0-2042-2521-7", ismn.format());
        assertEquals("9790204225217", ismn.checksum().toGTIN().normalizedValue());
    }

    @Test
    public void testISMN4() throws Exception {
        ISMN ismn = new ISMN().set("ISMN 979-0-001-04476-9").normalize().verify();
        assertEquals("9790001044769", ismn.normalizedValue());
        assertEquals("979-0-001-04476-9", ismn.format());
        assertEquals("9790001044769", ismn.checksum().toGTIN().normalizedValue());
    }

    @Test
    public void testISMNChecksum() throws Exception {
        ISMN ismn = new ISMN().set("979-0-3452-4680").checksum().normalize().verify();
        assertEquals("9790345246805", ismn.normalizedValue());
        assertEquals("979-0-345246805", ismn.format());
        assertEquals("9790345246805", ismn.toGTIN().normalizedValue());
    }

}
