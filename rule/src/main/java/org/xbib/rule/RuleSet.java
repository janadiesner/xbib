package org.xbib.rule;

import java.util.HashSet;

public class RuleSet extends HashSet<Rule> {

    public boolean apply(RuleContext ruleContext, Binding binding) {
        boolean eval = false;
        for (Rule rule : this) {
            eval = eval || rule.apply(ruleContext, binding);
        }
        return eval;
    }

}
