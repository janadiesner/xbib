package org.xbib.template.handlebars.i248;

import org.junit.Test;
import org.xbib.template.handlebars.EscapingStrategy;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Template;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class Issue248 {

    @Test
    public void defaultEscape() throws IOException {
        Template template = new Handlebars().compileInline("{{this}}");

        assertEquals("&quot;Escaping&quot;", template.apply("\"Escaping\""));
        assertEquals("", template.apply(null));
        assertEquals("", template.apply(""));
    }

    @Test
    public void csvEscape() throws IOException {
        Template template = new Handlebars().with(EscapingStrategy.CSV).compileInline("{{this}}");

        assertEquals("\"\"\"Escaping\"\"\"", template.apply("\"Escaping\""));
        assertEquals("", template.apply(null));
        assertEquals("", template.apply(""));
    }

    @Test
    public void xmlEscape() throws IOException {
        Template template = new Handlebars().with(EscapingStrategy.XML).compileInline("{{this}}");

        assertEquals("&lt;xml&gt;", template.apply("<xml>"));
        assertEquals("", template.apply(null));
        assertEquals("", template.apply(""));
    }

    @Test
    public void jsEscape() throws IOException {
        Template template = new Handlebars().with(EscapingStrategy.JS).compileInline("{{this}}");

        assertEquals("\\'javascript\\'", template.apply("'javascript'"));
        assertEquals("", template.apply(null));
        assertEquals("", template.apply(""));
    }
}
