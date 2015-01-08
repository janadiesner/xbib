package org.xbib.rule;

import java.util.Stack;

public class ExpressionParser {

    private static final Operations operations = Operations.INSTANCE;

    public static Expression parse(String expr) {
        Stack<Expression> stack = new Stack<>();
        String[] tokens = expr.split("\\s");
        for (int i = 0; i < tokens.length - 1; i++) {
            Operator op = operations.getOperation(tokens[i]);
            if (op != null) {
                i = op.copy().execute(tokens, i, stack);
            }
        }
        return stack.pop();
    }
}