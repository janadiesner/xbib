package org.xbib.sql.parser.expression.operators.relational;

import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.expression.ExpressionVisitor;

public class Between implements Expression {
	private Expression leftExpression;
	private boolean not = false;
	private Expression betweenExpressionStart;
	private Expression betweenExpressionEnd;

	public Expression getBetweenExpressionEnd() {
		return betweenExpressionEnd;
	}

	public Expression getBetweenExpressionStart() {
		return betweenExpressionStart;
	}

	public Expression getLeftExpression() {
		return leftExpression;
	}

	public boolean isNot() {
		return not;
	}

	public void setBetweenExpressionEnd(Expression expression) {
		betweenExpressionEnd = expression;
	}

	public void setBetweenExpressionStart(Expression expression) {
		betweenExpressionStart = expression;
	}

	public void setLeftExpression(Expression expression) {
		leftExpression = expression;
	}

	public void setNot(boolean b) {
		not = b;
	}

	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public String toString() {
		return leftExpression + " " + (not?"NOT ":"") + "BETWEEN "+
		betweenExpressionStart+" AND "+betweenExpressionEnd;
	}
}
