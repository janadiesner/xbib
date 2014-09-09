package org.xbib.rule;

import java.util.ArrayList;
import java.util.Map;

public class Rules extends ArrayList<Rule> {

    public boolean apply(RuleContext ruleContext, Map<String, ?> bindings) {
        boolean eval = false;
        for (Rule rule : this) {
            eval = eval || rule.apply(ruleContext, bindings);
        }
        return eval;
    }

}
