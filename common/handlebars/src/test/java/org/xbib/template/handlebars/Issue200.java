
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Issue200 extends AbstractTest {

    @Test
    public void actualBug() throws IOException {
        Handlebars h = newHandlebars();
        h.registerHelper("replaceHelperTest", new Helper<String>() {
            @Override
            public CharSequence apply(final String text,
                                      final Options options) {
                return "foo";
            }
        });
        Template t = h.compileInline("hello world: {{replaceHelperTest \"foobar\"}}");
        assertEquals("hello world: foo", t.apply(null));

        h.registerHelpers(new DynamicHelperExample());

        assertEquals("hello world: bar", t.apply(null));
    }
}
