package org.xbib.template.handlebars.i229;

import org.junit.Test;
import org.xbib.template.handlebars.AbstractTest;
import org.xbib.template.handlebars.Context;

import java.io.IOException;


public class Issue229 extends AbstractTest {

    @Test
    public void args() throws IOException {
        Context context = Context.newContext(null);
        context.data("data", new Object() {
            @SuppressWarnings("unused")
            public String getContext() {
                return "Ok!";
            }
        });
        shouldCompileTo("{{@data.context}}", context, "Ok!");
    }
}
