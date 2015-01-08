package org.xbib.rule;

import java.util.Stack;

public class Equals extends AbstractOperator {
    public Equals() {
        super("=");
    }

    @Override
    public Equals copy() {
        return new Equals();
    }

    @Override
    public int execute(final String[] tokens, int pos, Stack<Expression> stack) {
        if (pos - 1 >= 0 && tokens.length >= pos + 1) {
            String var = tokens[pos - 1];
            this.leftOperand = new Variable(var);
            this.rightOperand = BaseType.getBaseType(tokens[pos + 1]);
            stack.push(this);
            return pos + 1;
        }
        throw new IllegalArgumentException("Cannot assign value to variable");
    }

    @Override
    public boolean interpret(Binding binding) {
        Variable v = (Variable) this.leftOperand;
        Object obj = binding.get(v.getName());
        if (obj == null) {
            return false;
        }
        BaseType<?> type = (BaseType<?>) this.rightOperand;
        if (type.getType().equals(obj.getClass())) {
            if (type.getValue().equals(obj)) {
                return true;
            }
        }
        return false;
    }
}