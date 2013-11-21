
package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test serial number recognition
 *
 */
public class ISSNTest extends Assert {

    @Test
    public void testCorrectISSN() {
        try {
            ISSN issn = new ISSN("2193-5777");
            String s = issn.getStandardNumberValue();
            assertEquals(s, "21935777");
        } catch (InvalidStandardNumberException s) {
            assertTrue(Boolean.FALSE);
        }
    }
    
    @Test
    public void testCorrectISSN2() {
        try {
            ISSN issn = new ISSN("0044-2410");
            String s = issn.getStandardNumberValue();
            assertEquals(s, "00442410");
        } catch (InvalidStandardNumberException s) {
            assertTrue(Boolean.FALSE);
        }
    }

    @Test
    public void testCorrectISSN3() {
        try {
            ISSN issn = new ISSN("1934-791X");
            String s = issn.getStandardNumberValue();
            assertEquals(s, "1934791X");
        } catch (InvalidStandardNumberException s) {
            assertTrue(Boolean.FALSE);
        }
    }    
    
    @Test
    public void testWrongISSN() {
        try {
            ISSN issn = new ISSN("0949-3051"); // correct:  0949-3050 
            String s = issn.getStandardNumberValue();
        } catch (InvalidStandardNumberException s) {
            assertTrue(Boolean.TRUE);
            return;
        }
        assertTrue(Boolean.FALSE);
    }

    @Test
    public void testDirtyISSN() throws Exception {
        ISSN issn = new ISSN("ISSN 0936-0204");
        String s = issn.getStandardNumberValue();
        assertEquals("09360204", s);
    }

}
