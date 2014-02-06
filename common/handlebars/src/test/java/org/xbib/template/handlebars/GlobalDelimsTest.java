
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

public class GlobalDelimsTest extends AbstractTest {

    @Override
    protected Handlebars newHandlebars() {
        return super.newHandlebars().startDelimiter("<<").endDelimiter(">>");
    }

    @Test
    public void customDelims() throws IOException {
        shouldCompileTo("<<hello>>", $("hello", "hi"), "hi");
    }
}
