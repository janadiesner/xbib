
package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test international book number recognition
 *
 */
public class ISBNTest extends Assert {

    @Test
    public void testISBNTooShort() throws Exception {
        boolean b = false;
        try {
            String value = "12-7";
            new ISBN(value);
        } catch (InvalidStandardNumberException e) {
            b = true;
        }
        assertTrue(b);
    }

    @Test
    public void testDirty() throws Exception {
        String value = "ISBN 3-9803350-5-4 kart. : DM 24.00";
        ISBN isbn = new ISBN(value, false);
        assertEquals("3980335054", isbn.getStandardNumberValue());
    }

    @Test
    public void testTooShort() throws Exception {
        String value = "ISBN";
        boolean b = false;
        try {
            new ISBN(value, false);
        } catch (InvalidStandardNumberException e) {
            b = true;
        }
        assertTrue(b);
    }

    @Test
    public void fixChecksum() throws Exception {
        String value = "3616065810";
        ISBN isbn = new ISBN(value, false, true);
        assertEquals("361606581X", isbn.getStandardNumberValue());
    }

    @Test
    public void testEAN() throws Exception {
        String value = "978-3-551-75213-0";
        EAN ean = new EAN(value, false);
        assertEquals("9783551752130", ean.getStandardNumberValue());
    }

    @Test
    public void testWrongAndDirtyEAN() throws Exception {
        // correct ISBN-10 is 3-451-04112-X
        String value = "ISBN ISBN 3-451-4112-X kart. : DM 24.80";
        boolean b = false;
        try {
            ISBN isbn = new ISBN(value, true, true);
        } catch (InvalidStandardNumberException e) {
            b = true;
        }
        assertTrue(b);
    }
}
