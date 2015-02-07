package org.xbib.trie.ahocorasick;

/**
 * Simple interface for mapping bytes to States.
 */
interface EdgeList<O> {

    State<O> get(char ch);

    void put(char ch, State<O> state);

    char[] keys();
}
