
package org.xbib.template.handlebars.io;

import org.junit.Test;
import org.xbib.template.handlebars.Handlebars;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CompositeTemplateLoaderTest {

    private CompositeTemplateLoader loader =
            new CompositeTemplateLoader(
                    new ClassPathTemplateLoader(),
                    new FileTemplateLoader("src/test/resources/inheritance")
            );

    @Test
    public void handlebarsWithCompositeLoader() throws IOException {
        Handlebars handlebars = new Handlebars()
                .with(loader);
        assertNotNull(handlebars.compile("template"));
        assertNotNull(handlebars.compile("home"));
    }

    @Test
    public void handlebarsWithTemplateLoaders() throws IOException {
        Handlebars handlebars = new Handlebars()
                .with(
                        new ClassPathTemplateLoader(),
                        new FileTemplateLoader("src/test/resources/inheritance")
                );
        assertNotNull(handlebars.compile("template"));
        assertNotNull(handlebars.compile("home"));
    }

    @Test
    public void sourceAtCp() throws IOException {
        assertNotNull(loader.sourceAt("template"));
    }

    @Test
    public void resolveSourceAtCp() throws IOException {
        assertEquals("/template.hbs", loader.resolve("template"));
    }

    @Test
    public void sourceAtFs() throws IOException {
        assertNotNull(loader.sourceAt("home"));
    }

    @Test
    public void resolveSourceAtFs() throws IOException {
        assertEquals(new File("src/test/resources/inheritance", "home.hbs").getPath(),
                loader.resolve("home"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getPrefix() throws IOException {
        loader.getPrefix();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSuffix() throws IOException {
        loader.getSuffix();
    }

    @Test
    public void getDelegates() throws IOException {
        Iterable<TemplateLoader> delegates = loader.getDelegates();
        assertNotNull(delegates);
        Iterator<TemplateLoader> iterator = delegates.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.next() instanceof ClassPathTemplateLoader);
        assertTrue(iterator.next() instanceof FileTemplateLoader);
    }
}
