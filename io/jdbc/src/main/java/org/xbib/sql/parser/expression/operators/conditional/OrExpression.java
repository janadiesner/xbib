package org.xbib.sql.parser.expression.operators.conditional;

import org.xbib.sql.parser.expression.BinaryExpression;
import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.expression.ExpressionVisitor;

public class OrExpression extends BinaryExpression {
	public OrExpression(Expression leftExpression, Expression rightExpression) {
		setLeftExpression(leftExpression);
		setRightExpression(rightExpression);
	}
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public String getStringExpression() {
		return "OR";
	}
}
