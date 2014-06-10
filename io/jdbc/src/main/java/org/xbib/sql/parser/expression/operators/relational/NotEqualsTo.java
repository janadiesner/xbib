package org.xbib.sql.parser.expression.operators.relational;

import org.xbib.sql.parser.expression.BinaryExpression;
import org.xbib.sql.parser.expression.ExpressionVisitor;

public class NotEqualsTo extends BinaryExpression {
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public String getStringExpression() {
		return "<>";
	}
}
