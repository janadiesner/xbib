package org.xbib.template.handlebars.i250;

import org.junit.Test;
import org.xbib.template.handlebars.AbstractTest;

public class Issue250 extends AbstractTest {

    @Test
    public void partialWithCustomContextLostParentContext() throws Exception {
        shouldCompileToWithPartials("{{> share page}}",
                $("p", "parent", "page", $("name", "share")),
                $("share", "{{p}}"),
                "parent");
    }

    @Test
    public void partialWithDefaultContextLostParentContext() throws Exception {
        shouldCompileToWithPartials("{{> share}}",
                $("p", "parent", "page", $("name", "share")),
                $("share", "{{p}}"),
                "parent");
    }
}
