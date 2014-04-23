package org.xbib.template.handlebars;

import org.junit.Test;
import org.xbib.template.handlebars.io.StringTemplateSource;

import static org.junit.Assert.assertEquals;

public class AbstractTemplateSourceTest {

    @Test
    public void testHashCode() {
        assertEquals(new StringTemplateSource("file", "abc").hashCode(),
                new StringTemplateSource("file", "abc").hashCode());
    }

    @Test
    public void testEqualsSameRef() {
        StringTemplateSource source1 = new StringTemplateSource("file", "abc");
        StringTemplateSource source2 = new StringTemplateSource("file", "abc");
        assertEquals(source1, source1);
        assertEquals(source1, source2);
    }
}
