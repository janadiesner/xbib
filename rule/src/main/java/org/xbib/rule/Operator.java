package org.xbib.rule;

import java.util.Stack;

public interface Operator extends Expression {

    Operator copy();

    String getSymbol();

    int execute(final String[] tokens, final int pos, final Stack<Expression> stack);
}
