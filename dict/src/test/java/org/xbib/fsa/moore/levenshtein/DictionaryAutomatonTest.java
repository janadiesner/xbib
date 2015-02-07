package org.xbib.fsa.moore.levenshtein;

import org.testng.annotations.Test;
import org.xbib.fsa.moore.dictionary.DictionaryAutomaton;

import java.util.Arrays;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DictionaryAutomatonTest {

    @Test
    public void test() {
        DictionaryAutomaton da = new DictionaryAutomaton(Arrays.asList("Hello", "World"));
        assertTrue(da.accept("Hello"));
        assertFalse(da.accept("foobar"));
    }
}
