
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class EachKeyTest extends AbstractTest {

    public static class Blog {

        private String title;

        private String body;

        public Blog(final String title, final String body) {
            this.title = title;
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }
    }

    @Test
    public void eachKeyWithString() throws IOException {
        shouldCompileTo("{{#each this}}{{@key}} {{/each}}", "String", " ");
    }

    @Test
    public void eachKeyWithInt() throws IOException {
        shouldCompileTo("{{#each this}}{{@key}} {{/each}}", 7, "");
    }

    @Test
    public void eachKeyWithJavaBean() throws IOException {
        Blog blog = new Blog("Handlebars.java", "...");
        String result = compile("{{#each this}}{{@key}}: {{this}} {{/each}}").apply(blog);

        String expected1 = "body: ... title: Handlebars.java ";
        String expected2 = "title: Handlebars.java body: ... ";
        assertTrue(result.equals(expected1) || result.equals(expected2));
    }

    @Test
    public void eachKeyWithHash() throws IOException {
        Map<String, Object> hash = new LinkedHashMap<String, Object>();
        hash.put("body", "...");
        hash.put("title", "Handlebars.java");
        shouldCompileTo("{{#each this}}{{@key}}: {{this}} {{/each}}", hash,
                "body: ... title: Handlebars.java ");
    }
}
