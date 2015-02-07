package org.xbib.trie.ahocorasick;

/**
 * Linked list implementation of the EdgeList should be less memory-intensive.
 */
class SparseEdgeList<O> implements EdgeList<O> {

    private Cons<O> head;

    public SparseEdgeList() {
        head = null;
    }

    public State<O> get(char c) {
        Cons<O> cons = head;
        while (cons != null) {
            if (cons.c == c) {
                return cons.s;
            }
            cons = cons.next;
        }
        return null;
    }

    public void put(char c, State<O> s) {
        this.head = new Cons<O>(c, s, head);
    }

    public char[] keys() {
        int length = 0;
        Cons<O> c = head;
        while (c != null) {
            length++;
            c = c.next;
        }
        char[] result = new char[length];
        c = head;
        int j = 0;
        while (c != null) {
            result[j] = c.c;
            j++;
            c = c.next;
        }
        return result;
    }

    private static class Cons<O> {
        char c;
        State<O> s;
        Cons<O> next;

        public Cons(char c, State<O> s, Cons<O> next) {
            this.c = c;
            this.s = s;
            this.next = next;
        }
    }

}
