
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

public class IfCondHelperTest extends AbstractTest {

    @Test
    public void ifCondTest() throws IOException {
        shouldCompileTo("{{#ifCond value \"==\" \"x\"}}true{{else}}false{{/ifCond}}", $("value", "x"), "true");

        shouldCompileTo("{{#ifCond value \">\" 1}}true{{else}}false{{/ifCond}}", $("value", 2), "true");
        shouldCompileTo("{{#ifCond value \">\" 2}}true{{else}}false{{/ifCond}}", $("value", 1), "false");

        shouldCompileTo("{{#ifCond value \">\" 1}}true{{else}}{{/ifCond}}", $("value", 2), "true");
        shouldCompileTo("{{#ifCond value \">\" 2}}true{{else}}{{/ifCond}}", $("value", 1), "");
    }

}
