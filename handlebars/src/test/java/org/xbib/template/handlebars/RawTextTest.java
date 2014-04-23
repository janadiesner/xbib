
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Template#text()}
 */
public class RawTextTest extends AbstractTest {

    @Test
    public void plainText() throws IOException {
        assertEquals("Plain Text!", compile("Plain Text!").text());
    }

    @Test
    public void var() throws IOException {
        assertEquals("hello {{var}}!", compile("hello {{var}}!").text());
    }

    @Test
    public void varAmp() throws IOException {
        assertEquals("hello {{&var}}!", compile("hello {{& var}}!").text());
    }

    @Test
    public void var3() throws IOException {
        assertEquals("hello {{{var}}}!", compile("hello {{{ var }}}!").text());
    }

    @Test
    public void emptySection() throws IOException {
        assertEquals("hello {{#section}} {{/section}}!", compile("hello {{#section}} {{/section}}!")
                .text());
    }

    @Test
    public void section() throws IOException {
        assertEquals("hello {{#section}} hello {{/section}}!",
                compile("hello {{#section}} hello {{/section}}!").text());
    }

    @Test
    public void invertedEmptySection() throws IOException {
        assertEquals("hello {{^section}} {{/section}}!", compile("hello {{^section}} {{/section}}!")
                .text());
    }

    @Test
    public void invertedSection() throws IOException {
        assertEquals("hello {{^section}} hello {{var}}! {{/section}}!",
                compile("hello {{^section}} hello {{var}}! {{/section}}!")
                        .text());
    }

    @Test
    public void partial() throws IOException {
        assertEquals("hello {{>user}}!", compile("hello {{>user}}!", $(), $("user", "{{user}}"))
                .text());
    }

    @Test
    public void helper() throws IOException {
        assertEquals("hello {{with context arg0 hash=hash0}}!",
                compile("hello {{with context arg0 hash=hash0}}!")
                        .text());
    }

    @Test
    public void blockHelper() throws IOException {
        assertEquals("hello {{#with context arg0 hash=hash0}}hah{{/with}}!",
                compile("hello {{#with context arg0 hash=hash0}}hah{{/with}}!")
                        .text());
    }
}
