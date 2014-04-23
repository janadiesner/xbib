
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

public class SelectHelperTest extends AbstractTest {

    @Test
    public void selectTest() throws IOException {
        shouldCompileTo("{{select value \"==\" \"x\" 1 0}}", $("value", "x"), "1");
        shouldCompileTo("{{select value \"==\" \"x\" value \"\"}}", $("value", "x"), "x");
        shouldCompileTo("{{select value \"!=\" \"x\" value \"\"}}", $("value", "x"), "");

        shouldCompileTo("{{select value \">\" 1 true false}}", $("value", 2), "true");
        shouldCompileTo("{{select value \">\" 2 0 value}}", $("value", 1), "1");

    }

}
