package org.xbib.sql.parser.expression;

public interface Expression {
	void accept(ExpressionVisitor visitor);
}
