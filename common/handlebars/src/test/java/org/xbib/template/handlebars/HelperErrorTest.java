
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

public class HelperErrorTest extends AbstractTest {

    Hash source = $(
            "helper", "\n{{#block}} {{/block}}",
            "embedded", "\n{{#embedded}} {{/embedded}}",
            "basic", "\n{{basic}}",
            "notfoundblock", "\n{{#notfound hash=x}}{{/notfound}}",
            "notfound", "\n{{notfound hash=x}}"
    );

    @Test(expected = HandlebarsException.class)
    public void block() throws IOException {
        parse("helper");
    }

    @Test(expected = HandlebarsException.class)
    public void notfound() throws IOException {
        parse("notfound");
    }

    @Test(expected = HandlebarsException.class)
    public void notfoundblock() throws IOException {
        parse("notfoundblock");
    }

    @Test(expected = HandlebarsException.class)
    public void basic() throws IOException {
        parse("basic");
    }

    @Test(expected = HandlebarsException.class)
    public void embedded() throws IOException {
        parse("embedded");
    }

    private Object parse(final String uri) throws IOException {
        try {
            Hash helpers = $("basic", new Helper<Object>() {
                @Override
                public CharSequence apply(final Object context, final Options options)
                        throws IOException {
                    throw new IllegalArgumentException("missing parameter: '0'.");
                }
            });
            shouldCompileTo((String) source.get(uri), $, helpers, "must fail");
            throw new IllegalStateException("An error is expected");
        } catch (HandlebarsException ex) {
            throw ex;
        }
    }
}
