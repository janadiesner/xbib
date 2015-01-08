package org.xbib.rule;

import java.util.Stack;

public class And extends AbstractOperator {
    public And() {
        super("AND");
    }

    @Override
    public And copy() {
        return new And();
    }

    @Override
    public int execute(String[] tokens, int pos, Stack<Expression> stack) {
        Expression left = stack.pop();
        int i = executeNext(tokens, pos + 1, stack);
        Expression right = stack.pop();
        this.leftOperand = left;
        this.rightOperand = right;
        stack.push(this);
        return i;
    }

    @Override
    public boolean interpret(Binding binding) {
        return leftOperand.interpret(binding) && rightOperand.interpret(binding);
    }
}
