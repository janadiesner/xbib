package org.xbib.sql.parser.expression;

public class LongValue implements Expression {
	private long value;
	private String stringValue;

	public LongValue(String value) {
	    if (value.charAt(0) == '+') {
	        value = value.substring(1);
	    }
		this.value = Long.parseLong(value);
		setStringValue(value);
	}

	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public long getValue() {
		return value;
	}

	public void setValue(long d) {
		value = d;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String string) {
		stringValue = string;
	}

	public String toString() {
		return getStringValue();
	}
}
