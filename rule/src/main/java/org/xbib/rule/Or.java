package org.xbib.rule;

import java.util.Stack;

public class Or extends AbstractOperator {
    public Or() {
        super("OR");
    }

    @Override
    public Or copy() {
        return new Or();
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
        return leftOperand.interpret(binding) || rightOperand.interpret(binding);
    }
}
