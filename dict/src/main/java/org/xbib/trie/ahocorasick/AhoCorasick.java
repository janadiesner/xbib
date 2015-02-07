package org.xbib.trie.ahocorasick;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * An implementation of the Aho-Corasick string searching automaton. This
 * implementation of the <a
 * href="http://portal.acm.org/citation.cfm?id=360855&dl=ACM&coll=GUIDE"
 * target="_blank">Aho-Corasick</a> algorithm is optimized to work with chars.
 * </p>
 * <p/>
 * <p>
 * Example usage: <code><pre>
 * AhoCorasick tree = new AhoCorasick();
 * tree.add("hello");
 * tree.add("world");
 * tree.prepare();
 * <p/>
 * Iterator searcher = tree.progressiveSearch("hello world");
 * while (searcher.hasNext()) {
 * SearchResult result = searcher.next();
 * System.out.println(result.getOutputs());
 * System.out.println("Found at index: " + result.getLastIndex());
 * }
 * </pre></code>
 * </p>
 */
public class AhoCorasick {

    private State<String> root;

    private boolean prepared;

    /**
     * Default constructor, assuming that the outputs will be String.
     */
    public AhoCorasick() {
        this.root = new State<String>(0);
        this.prepared = false;
    }

    public void add(String keyword, String output) {
        add(keyword.toCharArray(), output);
    }

    public void add(String keyword) {
        add(keyword.toCharArray());
    }

    /**
     * Adds a new keyword with the associated String as output. During search,
     * if the keyword is matched, output will be one of the yielded elements in
     * SearchResults.getOutputs().
     */
    public void add(char[] keyword) {
        add(keyword, new String(keyword));
    }

    /**
     * Adds a new keyword with the given output. During search, if the keyword
     * is matched, output will be one of the yielded elements in
     * SearchResults.getOutputs().
     */
    public void add(char[] keyword, String output) {
        if (this.prepared) {
            throw new IllegalStateException("can't add keywords after prepare() is called");
        }
        State<String> lastState = this.root.extendAll(keyword);
        lastState.addOutput(output);
    }

    /**
     * Prepares the automaton for searching. This must be called before any
     * searching().
     */
    public void prepare() {
        this.prepareFailTransitions();
        this.prepared = true;
    }

    public Iterator<SearchResult> progressiveSearch(String inputText) {
        return progressiveSearch(inputText.toCharArray());
    }

    /**
     * Starts a new search, and returns an Iterator of SearchResults.
     */
    public Iterator<SearchResult> progressiveSearch(char[] chars) {
        return new Searcher(this, this.startSearch(chars));
    }

    public List<Result> completeSearch(String inputText,
                                       boolean allowOverlapping) {
        return completeSearch(inputText.toCharArray(), allowOverlapping);
    }

    /**
     * It make a whole new search, and it returns all the OutputResult objects,
     * ordered by the startIndex attribute. If the parameter allowOverlapping is
     * false, the overlapped outputs will be removed.
     */
    public List<Result> completeSearch(char[] chars,
                                       boolean allowOverlapping) {
        List<Result> result;
        Searcher searcher = new Searcher(this, this.startSearch(chars));
        result = recollectOutputResults(searcher);
        sortOutputResults(result);
        if (!allowOverlapping) {
            removeOverlapping(result);
        }
        return result;
    }

    /**
     * The non-overlapping outputs are taken to be the left-most and
     * longest-matching, according to the following definitions. An output with
     * span <code>(start1,last1)</code> overlaps an output with span
     * <code>(start2,last2)</code> if and only if either end points of the
     * second output lie within the first chunk:
     * <ul>
     * <li> <code>start1 <= start2 < last1</code>, or
     * <li> <code>start1 < last2 <= last1</code>.
     * </ul>
     * <p/>
     * For instance, <code>(0,1)</code> and <code>(1,3)</code> do not overlap,
     * but <code>(0,1)</code> overlaps <code>(0,2)</code>, <code>(1,2)</code>
     * overlaps <code>(0,2)</code>, and <code>(1,7)</code> overlaps
     * <code>(2,3)</code>.
     * <p/>
     * <p/>
     * An output <code>output1=(start1,last1)</code> dominates another output
     * <code>output2=(start2,last2)</code> if and only if the outputs overlap
     * and:
     * <p/>
     * <ul>
     * <li> <code>start1 &lt; start2</code> (leftmost), or
     * <li> <code>start1 == start2</code> and <code>last1 &gt; last2</code>
     * (longest).
     * </ul>
     */
    void removeOverlapping(List<Result> results) {
        int currentIndex = 0;
        Result current;
        Result next;
        while (currentIndex < (results.size() - 1)) {
            current = results.get(currentIndex);
            next = results.get(currentIndex + 1);
            if (!current.isOverlapped(next)) {
                currentIndex++;
            } else if (current.dominate(next)) {
                results.remove(currentIndex + 1);
            } else {
                results.remove(currentIndex);
            }
        }
    }

    private void prepareFailTransitions() {
        LinkedList<State<String>> q = new LinkedList<>();
        char[] keys = this.root.keys();
        State<String> state;
        State<String> r;
        State<String> s;
        for (char key : keys) {
            state = this.root.get(key);
            state.setFail(this.root);
            q.push(state);
        }
        while (!q.isEmpty()) {
            state = q.pop();
            keys = state.keys();
            for (char key : keys) {
                s = state.get(key);
                q.add(s);
                r = state.getFail();
                while (r.get(key) == null) {
                    r = r.getFail();
                }
                s.setFail(r.get(key));
                s.getOutputs().addAll(r.get(key).getOutputs());
            }
        }
    }

    /**
     * Returns the root of the tree. Package protected, since the user probably
     * shouldn't touch this.
     */
    State<String> getRoot() {
        return this.root;
    }

    /**
     * Begins a new search using the raw interface. Package protected.
     */
    SearchResult startSearch(char[] chars) {
        if (!this.prepared) {
            throw new IllegalStateException(
                    "can't start search until prepare()");
        }

        return continueSearch(new SearchResult(this.root, chars, 0));
    }

    /**
     * Continues the search, given the initial state described by the
     * lastResult. Package protected.
     */
    SearchResult continueSearch(SearchResult lastResult) {
        char currentChar;
        SearchResult searchResult = null;
        char[] chars = lastResult.chars;
        State<String> state = lastResult.lastMatchedState;
        Integer currentIndex = lastResult.lastIndex;
        while (shouldContinueSearching(searchResult, currentIndex, chars)) {
            currentChar = chars[currentIndex];
            while (state.get(currentChar) == null) {
                state = state.getFail();
            }
            state = state.get(currentChar);
            if (!state.getOutputs().isEmpty()) {
                searchResult = new SearchResult(state, chars, currentIndex + 1);
            } else {
                currentIndex++;
            }
        }
        return searchResult;
    }

    private boolean shouldContinueSearching(SearchResult searchResult,
                                            Integer currentIndex, char[] inputText) {
        return searchResult == null && currentIndex != null
                && currentIndex < inputText.length;
    }

    private void sortOutputResults(List<Result> results) {
        Collections.sort(results, new Comparator<Result>() {
            @Override
            public int compare(Result o1, Result o2) {
                return o1.getStartIndex() - o2.getStartIndex();
            }
        });
    }

    private List<Result> recollectOutputResults(Searcher searcher) {
        Integer startIndex;
        SearchResult searchResult;
        List<Result> result = new LinkedList<Result>();
        while (searcher.hasNext()) {
            searchResult = searcher.next();
            for (String output : searchResult.getOutputs()) {
                startIndex = searchResult.lastIndex - output.length();
                result.add(new Result(output, startIndex,
                        searchResult.lastIndex));
            }
        }
        return result;
    }
}
