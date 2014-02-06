
package org.xbib.template.handlebars;

import mustache.specs.Spec;
import mustache.specs.SpecTest;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Collection;


public class CustomObjectTest extends SpecTest {

    public CustomObjectTest(final Spec spec) {
        super(spec);
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        return data(CustomObjectTest.class, "customObjects.yml");
    }

}
