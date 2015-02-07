package org.xbib.fsa.moore;

import java.util.Collection;

/**
 * A suggester based on an automaton.
 *
 */
public interface Suggester {

    /**
     * Get the automaton of this suggester
     * @return the automaton
     */
    Automaton getAutomaton();

    /**
     * Add new char sequence to the automaton
     * @param input the input
     */
    void add(CharSequence input);

    /**
     * Get simple suggestions for a given input from this suggester
     * @param input the input
     * @param exact if true, suggestions will include exact matches, 
     *    if the given input is a word in the dictionary
     * @return a collection of simple suggestions in form of character sequences
     */
    Collection<CharSequence> getSuggestionsFor(CharSequence input, boolean exact, int maxlevel);
}
