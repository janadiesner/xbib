package org.xbib.trie.ahocorasick;

import java.util.Set;

/**
 * <p>
 * Holds the result of the search so far. Includes the outputs where the search
 * finished as well as the last index of the matching.
 * </p>
 * <p/>
 * <p>
 * (Internally, it also holds enough state to continue a running search, though
 * this is not exposed for public use.)
 * </p>
 */
public class SearchResult {

    State<String> lastMatchedState;

    char[] chars;

    /**
     * The index where the search terminates. Note that this is one byte after
     * the last matching character.
     */
    int lastIndex;

    SearchResult(State<String> s, char[] cs, int i) {
        this.lastMatchedState = s;
        this.chars = cs;
        this.lastIndex = i;
    }

    /**
     * Returns a list of the outputs of this match.
     */
    public Set<String> getOutputs() {
        return lastMatchedState.getOutputs();
    }
}
