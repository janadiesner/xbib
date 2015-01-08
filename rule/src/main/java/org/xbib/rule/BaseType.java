package org.xbib.rule;

public class BaseType<T> implements Expression {
    private T value;
    private Class<T> type;

    public BaseType(T value, Class<T> type) {
        this.value = value;
        this.type = type;
    }

    public T getValue() {
        return this.value;
    }

    public Class<T> getType() {
        return this.type;
    }

    @Override
    public boolean interpret(Binding binding) {
        return true;
    }

    public static BaseType<?> getBaseType(String string) {
        if (string == null) {
            throw new IllegalArgumentException("The provided string must not be null");
        }
        if ("true".equals(string) || "false".equals(string)) {
            return new BaseType<>(Boolean.getBoolean(string), Boolean.class);
        } else if (string.startsWith("'")) {
            return new BaseType<>(string, String.class);
        } else if (string.contains(".")) {
            return new BaseType<>(Float.parseFloat(string), Float.class);
        } else {
            return new BaseType<>(Integer.parseInt(string), Integer.class);
        }
    }
}
