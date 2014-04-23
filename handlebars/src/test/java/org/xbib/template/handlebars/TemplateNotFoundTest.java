
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

public class TemplateNotFoundTest {

    @Test(expected = FileNotFoundException.class)
    public void templateNotFound() throws IOException {
        Handlebars handlebars = new Handlebars();
        handlebars.compile("template.hbs");
    }

    @Test(expected = HandlebarsException.class)
    public void partialNotFound() throws IOException {
        Handlebars handlebars = new Handlebars();
        handlebars.compileInline("{{> text}}").apply(null);
    }
}
