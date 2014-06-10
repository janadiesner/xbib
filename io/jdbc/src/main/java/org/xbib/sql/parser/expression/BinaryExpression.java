package org.xbib.sql.parser.expression;

public abstract class BinaryExpression implements Expression {
	private Expression leftExpression;
	private Expression rightExpression;
	private boolean not = false;

	public BinaryExpression() {
	}

	public Expression getLeftExpression() {
		return leftExpression;
	}

	public Expression getRightExpression() {
		return rightExpression;
	}

	public void setLeftExpression(Expression expression) {
		leftExpression = expression;
	}

	public void setRightExpression(Expression expression) {
		rightExpression = expression;
	}

	public void setNot() {
		not = true;
	}

	public boolean isNot() {
		return not;
	}

	public String toString() {
		return (not? "NOT ":"") + getLeftExpression()+" "+getStringExpression()+" "+getRightExpression();
	}

	public abstract String getStringExpression();
	
}
