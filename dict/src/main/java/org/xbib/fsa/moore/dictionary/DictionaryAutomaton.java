package org.xbib.fsa.moore.dictionary;

import org.xbib.fsa.moore.AbstractAutomaton;
import org.xbib.fsa.moore.CompactState;
import org.xbib.fsa.moore.State;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class DictionaryAutomaton extends AbstractAutomaton<Character, CharSequence> {

    private final Set<Character> alphabet = new TreeSet<Character>();

    public DictionaryAutomaton(Iterator<CharSequence> words) {
        createInitialInput(words);
    }

    public DictionaryAutomaton(Iterable<CharSequence> words) {
        createInitialInput(words);
    }

    @Override
    protected State<Character, CharSequence> newState() {
        return new CompactState<Character, CharSequence>();
    }

    /**
     * Set the name of this dictionary automaton
     *
     * @param name name
     * @return this automaton
     */
    public DictionaryAutomaton name(String name) {
        setName(name);
        return this;
    }

    /**
     * Add a word to the dictionary automaton.
     *
     * @param word word
     * @return the DictionaryAutomaton
     */
    public DictionaryAutomaton add(CharSequence word) {
        int len = word.length();
        Character[] path = new Character[len];
        for (int i = 0; i < len; i++) {
            path[i] = word.charAt(i);
            alphabet.add(path[i]);
        }
        add(path, word);
        return this;
    }

    /**
     * Gets the list of all unique characters in the dictionary, which
     * is also known as the alphabet.
     *
     * @return alphabet as a Collection of Characters
     */
    @Override
    public Set<Character> getAlphabet() {
        return alphabet;
    }

    public boolean accept(CharSequence word) {
        State<Character,CharSequence> state = getCurrentState();
        for (int i = 0; i < word.length(); i++) {
            Character ch = word.charAt(i);
            state = state.getNextState(ch);
            if (state == null) {
                return false;
            }
        }
        return state.isAccept();
    }

    private void createInitialInput(Iterator<CharSequence> words) {
        while (words.hasNext()) {
            add(words.next());
        }
    }

    private void createInitialInput(Iterable<CharSequence> words) {
        for (CharSequence word : words) {
            add(word);
        }
    }
}
