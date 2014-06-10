package org.xbib.sql.parser.expression;

public class NullValue implements Expression {
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public String toString() {
		return "NULL";
	}
}
