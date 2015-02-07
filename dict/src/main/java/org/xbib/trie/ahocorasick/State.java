package org.xbib.trie.ahocorasick;

import java.util.HashSet;
import java.util.Set;

/**
 * A state represents an element in the Aho-Corasick tree.
 */
class State<O> {

    private int depth;
    private EdgeList<O> edgeList;
    private State<O> fail;
    private Set<O> outputs;

    public State(int depth) {
        this.depth = depth;
        this.edgeList = new SparseEdgeList<O>();
        this.fail = null;
        this.outputs = new HashSet<>();
    }

    public State<O> extend(char c) {
        if (this.edgeList.get(c) != null) {
            return this.edgeList.get(c);
        }
        State<O> nextState = new State<O>(this.depth + 1);
        this.edgeList.put(c, nextState);
        return nextState;
    }

    public State<O> extendAll(char[] chars) {
        State<O> state = this;
        for (char aChar : chars) {
            if (state.edgeList.get(aChar) != null) {
                state = state.edgeList.get(aChar);
            } else {
                state = state.extend(aChar);
            }
        }
        return state;
    }

    /**
     * Returns the size of the tree rooted at this State. Note: do not call this
     * if there are loops in the edgelist graph, such as those introduced by
     * AhoCorasick.prepare().
     */
    public int size() {
        char[] keys = edgeList.keys();
        int result = 1;
        for (char key : keys) {
            result += edgeList.get(key).size();
        }
        return result;
    }

    public State<O> get(char c) {
        State<O> s = this.edgeList.get(c);
        if (s == null && isRoot()) {
            s = this;
        }
        return s;
    }

    public void put(char c, State<O> s) {
        this.edgeList.put(c, s);
    }

    public char[] keys() {
        return this.edgeList.keys();
    }

    public State<O> getFail() {
        return this.fail;
    }

    public void setFail(State<O> f) {
        this.fail = f;
    }

    public void addOutput(O o) {
        this.outputs.add(o);
    }

    public Set<O> getOutputs() {
        return this.outputs;
    }

    public Boolean isRoot() {
        return depth == 0;
    }

    public int getDepth() {
        return depth;
    }
}
