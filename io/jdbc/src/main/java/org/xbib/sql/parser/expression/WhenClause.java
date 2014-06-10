package org.xbib.sql.parser.expression;

public class WhenClause implements Expression {

	private Expression whenExpression;
	private Expression thenExpression;
	
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public Expression getThenExpression() {
		return thenExpression;
	}
	public void setThenExpression(Expression thenExpression) {
		this.thenExpression = thenExpression;
	}
	public Expression getWhenExpression() {
		return whenExpression;
	}
	public void setWhenExpression(Expression whenExpression) {
		this.whenExpression = whenExpression;
	}
	
	public String toString() {
		return "WHEN "+whenExpression+" THEN "+thenExpression;
	}
}
