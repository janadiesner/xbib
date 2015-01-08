package org.xbib.rule;

import java.util.HashMap;
import java.util.Map;

public enum Operations {

    INSTANCE;

    private final Map<String, Operator> operations = new HashMap<String, Operator>();

    public void registerOperation(Operator op) {
        if (!operations.containsKey(op.getSymbol())) {
            operations.put(op.getSymbol(), op);
        }
    }

    public Operator getOperation(String symbol) {
        return this.operations.get(symbol);
    }

}