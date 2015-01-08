package org.xbib.rule;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    private List<Expression> expressions;
    private Dispatcher dispatcher;

    public static class Builder {
        private List<Expression> expressions = new ArrayList<>();
        private Dispatcher dispatcher = new NullDispatcher();

        public Builder with(Expression expr) {
            expressions.add(expr);
            return this;
        }

        public Builder then(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public Rule build() {
            return new Rule(expressions, dispatcher);
        }
    }

    private Rule(List<Expression> expressions, Dispatcher dispatcher) {
        this.expressions = expressions;
        this.dispatcher = dispatcher;
    }

    public boolean apply(RuleContext ruleContext, Binding binding) {
        boolean eval = false;
        for (Expression expression : expressions) {
            eval = expression.interpret(binding);
            if (eval) {
                dispatcher.fire(expression, binding);
            }
        }
        return eval;
    }
}