
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

public class EvalHelperTest extends AbstractTest {

    @Test
    public void eval() throws IOException {
        shouldCompileTo("{{eval this \"+\" this}}", 1, "2");
    }

    @Test
    public void evalConst() throws IOException {
        shouldCompileTo("{{eval 1 \"+\" 1}}", "", "2");
    }

    @Test
    public void evalInc() throws IOException {
        shouldCompileTo("{{eval this \"+\" 1}}", 1, "2");
    }
}
