package org.xbib.trie.ahocorasick;

public class StringOutputSizeCalculator implements OutputSizeCalculator {

    @Override
    public int calculateSize(Object output) {
        if (!(output instanceof String)) {
            throw new IllegalStateException(
                    "The output class must be java.lang.String");
        } else {
            return ((String) output).length();
        }
    }

}
