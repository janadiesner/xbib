package org.xbib.rule;

import java.util.Stack;

public abstract class Operation implements Expression {

    protected String symbol;

    protected Expression leftOperand = null;

    protected Expression rightOperand = null;

    public Operation(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }

    protected Integer findNextExpression(String[] tokens, int pos, Stack<Expression> stack) {
        Operations operations = Operations.INSTANCE;
        for (int i = pos; i < tokens.length; i++) {
            Operation op = operations.getOperation(tokens[i]);
            if (op != null) {
                op = op.copy();
                i = op.parse(tokens, i, stack);
                return i;
            }
        }
        return null;
    }

    public abstract Operation copy();

    public abstract int parse(final String[] tokens, final int pos, final Stack<Expression> stack);
}
