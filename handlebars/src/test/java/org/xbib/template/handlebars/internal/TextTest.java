
package org.xbib.template.handlebars.internal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextTest {

    @Test
    public void newText() {
        assertEquals("a", new Text("a").text());
    }

    @Test
    public void newTextSequence() {
        assertEquals("abc", new Text("abc").text());
    }

    @Test(expected = NullPointerException.class)
    public void newTextFail() {
        new Text(null);
    }

}
