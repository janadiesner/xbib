
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

public class Issue181 extends AbstractTest {

    @Test
    public void blockWithContent() throws IOException {
        shouldCompileTo("{{#block \"name\"}}block{{/block}}", $, "block");
    }

    @Test
    public void blockWithoutContent() throws IOException {
        shouldCompileTo("{{block \"name\"}}", $, "");
    }

    @Test
    public void partialBlockWithContent() throws IOException {
        shouldCompileTo("{{#partial \"name\"}}partial{{/partial}}{{#block \"name\"}}block{{/block}}",
                $, "partial");
    }

    @Test
    public void partialBlockWithoutContent() throws IOException {
        shouldCompileTo("{{#partial \"name\"}}partial{{/partial}}{{block \"name\"}}", $,
                "partial");
    }

    @Test
    public void partialWithoutContentBlockWithContent() throws IOException {
        shouldCompileTo("{{partial \"name\"}}{{#block \"name\"}}block{{/block}}", $, "");
    }
}
