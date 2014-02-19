package org.xbib.marc;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class FieldTest {

    @Test
    public void testFieldCollection() {
        Field f = new Field().tag("016").indicator("").subfieldId(null).data(null);
        Field f1 = new Field(f).subfieldId("1");
        Field f2 = new Field(f).subfieldId("2");
        Field f3 = new Field(f).subfieldId("3");
        FieldCollection c = new FieldCollection();
        c.add(f);
        c.add(f1);
        c.add(f2);
        c.add(f3);
        assertEquals(c.toSpec(), "016$123");
    }
}
