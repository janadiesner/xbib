
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;

public class ParsingErrorTest extends AbstractTest {

    Hash source =
            $("inbox/inbox", "{{value",
                    "block", "{{#block}}{{/nan}}",
                    "iblock", "{{#block}}invalid block",
                    "delim", "{{=<% %>=}} <%Hello",
                    "default", "{{> missingPartial}}",
                    "partial", "{{#value}}",
                    "invalidChar", "\n{{tag message.from \\\"user\\\"}}\n",
                    "root", "{{> p1}}",
                    "p1", "{{value",
                    "deep", "{{> deep1}}",
                    "deep1", " {{> deep2",
                    "unbalancedDelim", "{{=<%%>=}}",
                    "partialName", "{{> /user}}",
                    "partialName2", "{{> /layout/base}}",
                    "paramOrder", "{{f param hashx=1 param}}",
                    "idx1", "{{list[0]}}",
                    "idx2", "{{list.[0}}",
                    "idx3", "{{list.[]}}",
                    "idx4", "{{list.[}}");

    @Test(expected = HandlebarsException.class)
    public void correctPath() throws IOException {
        parse("inbox/inbox");
    }

    @Test(expected = HandlebarsException.class)
    public void missingPartial() throws IOException {
        parse("default");
    }

    @Test(expected = HandlebarsException.class)
    public void invalidChar() throws IOException {
        parse("invalidChar");
    }

    @Test(expected = HandlebarsException.class)
    public void level1() throws IOException {
        parse("root");
    }

    @Test(expected = HandlebarsException.class)
    public void level2() throws IOException {
        parse("deep");
    }

    @Test(expected = HandlebarsException.class)
    public void block() throws IOException {
        parse("block");
    }

    @Test(expected = HandlebarsException.class)
    public void unbalancedDelim() throws IOException {
        parse("unbalancedDelim");
    }

    @Test(expected = HandlebarsException.class)
    public void delim() throws IOException {
        parse("delim");
    }

    @Test(expected = HandlebarsException.class)
    public void paramOutOfOrder() throws IOException {
        parse("paramOrder");
    }

    @Test(expected = HandlebarsException.class)
    public void iblock() throws IOException {
        parse("iblock");
    }

    @Test(expected = HandlebarsException.class)
    public void tvar() throws IOException {
        parse("{{{tvar");
    }

    @Test(expected = HandlebarsException.class)
    public void tvarDelim() throws IOException {
        parse("{{=** **=}}**{tvar");
    }

    @Test(expected = HandlebarsException.class)
    public void ampvar() throws IOException {
        parse("{{&tvar");
    }

    @Test(expected = HandlebarsException.class)
    public void ampvarDelim() throws IOException {
        parse("{{=** **=}}**&tvar");
    }

    @Test(expected = HandlebarsException.class)
    public void missingId() throws IOException {
        parse("{{is");
    }

    @Test(expected = HandlebarsException.class)
    public void partialName() throws IOException {
        parse("partialName");
    }

    @Test(expected = HandlebarsException.class)
    public void partialName2() throws IOException {
        parse("partialName2");
    }

    @Test(expected = HandlebarsException.class)
    public void idx1() throws IOException {
        parse("idx1");
    }

    @Test(expected = HandlebarsException.class)
    public void idx2() throws IOException {
        parse("idx2");
    }

    @Test(expected = HandlebarsException.class)
    public void idx3() throws IOException {
        parse("idx3");
    }

    @Test(expected = HandlebarsException.class)
    public void idx4() throws IOException {
        parse("idx4");
    }

    private void parse(final String candidate) throws IOException {
        try {
            String input = (String) source.get(candidate);
            Template compiled = compile(input == null ? candidate : input, $(), source);
            compiled.apply(new Object());
            fail("An error is expected");
        } catch (HandlebarsException ex) {
            throw ex;
        }
    }
}
