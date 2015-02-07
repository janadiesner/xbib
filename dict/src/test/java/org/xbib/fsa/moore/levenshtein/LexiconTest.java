package org.xbib.fsa.moore.levenshtein;

import org.testng.annotations.Test;
import org.xbib.fsa.moore.dictionary.Lexicon;

import java.util.Arrays;

public class LexiconTest {

    @Test
    public void test() {
        Lexicon lex = new Lexicon();
        lex.add("breast cancer", Arrays.asList("breast neoplasm", ""));
        System.out.println(lex.lookup("breast cancer"));
    }
}
