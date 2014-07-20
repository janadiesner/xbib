package org.xbib.template.handlebars.io;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Unit test for {@link ClassPathTemplateLoader}.
 *
 */
public class FileTemplateLoaderTest {

    @Test
    public void sourceAt() throws IOException {
        TemplateLoader loader =
                new FileTemplateLoader(new File("src/test/resources"));
        TemplateSource source = loader.sourceAt("template");
        assertNotNull(source);
    }

    @Test
    public void subFolder() throws IOException {
        TemplateLoader loader =
                new FileTemplateLoader(new File("src/test/resources"), ".yml");
        TemplateSource source = loader.sourceAt("mustache/specs/comments");
        assertNotNull(source);
    }

    @Test
    public void subFolderwithDashAtBeginning() throws IOException {
        TemplateLoader loader =
                new FileTemplateLoader(new File("src/test/resources"), ".yml");
        TemplateSource source = loader.sourceAt("/mustache/specs/comments");
        assertNotNull(source);
    }

    @Test(expected = FileNotFoundException.class)
    public void failLocate() throws IOException {
        TemplateLoader loader =
                new FileTemplateLoader(new File("src/test/resources"));
        loader.sourceAt("notExist");
    }

    @Test
    public void setBasePath() throws IOException {
        TemplateLoader loader =
                new FileTemplateLoader(new File("src/test/resources/mustache/specs"), ".yml");
        TemplateSource source = loader.sourceAt("comments");
        assertNotNull(source);
    }

    @Test
    public void setBasePathWithDash() throws IOException {
        TemplateLoader loader =
                new FileTemplateLoader(new File("src/test/resources/mustache/specs/"), ".yml");
        TemplateSource source = loader.sourceAt("comments");
        assertNotNull(source);
    }

    @Test
    public void nullSuffix() throws IOException {
        assertEquals("suffix should be optional",
                new FileTemplateLoader("src/test/resources/", null).sourceAt("noextension")
                        .content());
    }

    @Test
    public void emptySuffix() throws IOException {
        assertEquals("suffix should be optional",
                new FileTemplateLoader("src/test/resources/", "").sourceAt("noextension")
                        .content());
    }
}
