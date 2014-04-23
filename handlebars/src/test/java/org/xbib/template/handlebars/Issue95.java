package org.xbib.template.handlebars;

import org.junit.Test;
import org.xbib.template.handlebars.io.ClassPathTemplateLoader;
import org.xbib.template.handlebars.io.URLTemplateLoader;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class Issue95 {

    @Test
    public void issue95() throws IOException {
        URLTemplateLoader loader = new ClassPathTemplateLoader("/issue95");

        Handlebars handlebars = new Handlebars(loader);
        handlebars.setInfiniteLoops(true);
        Template template = handlebars.compile("hbs/start");
        assertNotNull(template);
    }
}
