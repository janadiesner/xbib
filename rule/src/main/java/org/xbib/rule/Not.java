package org.xbib.rule;

import java.util.Stack;

public class Not extends AbstractOperator {
    public Not() {
        super("NOT");
    }

    public Not copy() {
        return new Not();
    }

    @Override
    public int execute(String[] tokens, int pos, Stack<Expression> stack) {
        int i = executeNext(tokens, pos + 1, stack);
        this.rightOperand = stack.pop();
        stack.push(this);
        return i;
    }

    @Override
    public boolean interpret(Binding binding) {
        return !this.rightOperand.interpret(binding);
    }
}