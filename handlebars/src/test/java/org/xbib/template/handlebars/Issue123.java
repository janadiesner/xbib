
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;

public class Issue123 extends AbstractTest {

    @Test
    public void spacesInBlock() throws IOException {
        shouldCompileTo("{{#if \"stuff\" }}Bingo{{/if}}", $, "Bingo");
        shouldCompileTo("{{#if \"stuff\"  }}Bingo{{/if}}", $, "Bingo");
        shouldCompileTo("{{#if \"stuff\"}}Bingo{{/if}}", $, "Bingo");

        shouldCompileTo("{{# if \"stuff\"}}Bingo{{/if}}", $, "Bingo");
        shouldCompileTo("{{#if \"stuff\"}}Bingo{{/ if}}", $, "Bingo");
        shouldCompileTo("{{# if \"stuff\" }}Bingo{{/ if }}", $, "Bingo");
    }

    @Test
    public void spacesInVar() throws IOException {
        shouldCompileTo("{{var}}", $, "");
        shouldCompileTo("{{ var}}", $, "");
        shouldCompileTo("{{var }}", $, "");
        shouldCompileTo("{{ var }}", $, "");
        shouldCompileTo("{{var x }}", $, $("var", ""), "");
    }
}
