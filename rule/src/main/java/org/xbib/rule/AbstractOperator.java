package org.xbib.rule;

import java.util.Stack;

public abstract class AbstractOperator implements Operator {

    protected String symbol;

    protected Expression leftOperand = null;

    protected Expression rightOperand = null;

    public AbstractOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }

    protected Integer executeNext(String[] tokens, int pos, Stack<Expression> stack) {
        for (int i = pos; i < tokens.length; i++) {
            Operator op = Operations.INSTANCE.getOperation(tokens[i]);
            if (op != null) {
                return op.copy().execute(tokens, i, stack);
            }
        }
        return null;
    }

}
