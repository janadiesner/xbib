
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Issue157 extends AbstractTest {

    @Test
    public void whitespacesAndSpecialCharactersInTemplateNames() throws IOException {
        Handlebars handlebars = new Handlebars();

        assertEquals("works!", handlebars.compile("space between").apply($));
    }
}
