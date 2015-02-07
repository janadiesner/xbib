package org.xbib.fsa.moore.dictionary;

import org.xbib.fsa.moore.AbstractAutomaton;
import org.xbib.fsa.moore.CompactState;
import org.xbib.fsa.moore.State;

import java.util.Collection;
import java.util.Set;

public class Lexicon extends AbstractAutomaton<Character, Collection<CharSequence>> {

    @Override
    protected State<Character, Collection<CharSequence>> newState() {
        return new CompactState<Character, Collection<CharSequence>>();
    }

    @Override
    public Set<Character> getAlphabet() {
        return null;
    }

    public Lexicon add(CharSequence word, Collection<CharSequence> elements) {
        int len = word.length();
        Character[] path = new Character[len];
        for (int i = 0; i < len; i++) {
            path[i] = word.charAt(i);
        }
        add(path, elements);
        return this;
    }

    public Collection<CharSequence> lookup(CharSequence word) {
        State<Character,Collection<CharSequence>> state = getCurrentState();
        for (int i = 0; i < word.length(); i++) {
            Character ch = word.charAt(i);
            state = state.getNextState(ch);
            if (state == null) {
                return null;
            }
        }
        return state.isAccept() ? state.getElement() : null;
    }
}
