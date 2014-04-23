
package org.xbib.template.handlebars;

import org.junit.Test;
import org.xbib.template.handlebars.context.FieldValueResolver;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ArrayTest extends AbstractTest {

    private static class Letter {
        private char letter;

        public Letter(final char letter) {
            this.letter = letter;
        }

        @Override
        public String toString() {
            return letter + "";
        }
    }

    @Test
    public void stringArray() throws IOException {
        Hash hash = $("list", new String[]{"w", "o", "r", "l", "d"});
        shouldCompileTo("Hello {{#list}}{{this}}{{/list}}!", hash, "Hello world!");
    }

    @Test
    public void objectArray() throws IOException {
        Hash hash = $("list", new Object[]{"w", "o", "r", "l", "d"});
        shouldCompileTo("Hello {{#list}}{{this}}{{/list}}!", hash, "Hello world!");
    }

    @Test
    public void eachArray() throws IOException {
        Hash hash = $("list", new Object[]{"w", "o", "r", "l", "d"});
        shouldCompileTo("Hello {{#each list}}{{this}}{{/each}}!", hash, "Hello world!");
    }

    @Test
    public void letterArray() throws IOException {
        Hash hash = $("list", new Letter[]{new Letter('w'), new Letter('o'),
                new Letter('r'), new Letter('l'), new Letter('d')});
        shouldCompileTo("Hello {{#list}}{{this}}{{/list}}!", hash, "Hello world!");
    }

    @Test
    public void arrayLength() throws IOException {
        Object[] array = {"1", 2, "3"};
        assertEquals(
                "3", compile("{{this.length}}").apply(
                HandlebarsContext.newBuilder(array).resolver(FieldValueResolver.INSTANCE).build()));
    }
}
