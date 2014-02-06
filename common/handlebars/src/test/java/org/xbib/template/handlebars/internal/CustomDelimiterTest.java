package org.xbib.template.handlebars.internal;

import org.junit.Test;
import org.xbib.template.handlebars.AbstractTest;

import static org.junit.Assert.assertEquals;

public class CustomDelimiterTest extends AbstractTest {
    @Test
    public void block() throws Exception {
        assertEquals("`*`#test`*`inside`*`/test`*`",
                compile("{{=`*` `*`=}}`*`#test`*`inside`*`/test`*`").text());
    }

    @Test
    public void partial() throws Exception {
        assertEquals("^^>test%%", compile("{{=^^ %%=}}^^>test%%", $(), $("test", "")).text());
    }

    @Test
    public void variable() throws Exception {
        assertEquals("+-+test-+-", compile("{{=+-+ -+-=}}+-+test-+-").text());
    }

    @Test
    public void variableUnescaped() throws Exception {
        assertEquals("+-+&test-+-", compile("{{=+-+ -+-=}}+-+&test-+-").text());
    }

    @Test
    public void tripleVariable() throws Exception {
        assertEquals("+-+{test}-+-", compile("{{=+-+ -+-=}}+-+{test}-+-").text());
    }
}
