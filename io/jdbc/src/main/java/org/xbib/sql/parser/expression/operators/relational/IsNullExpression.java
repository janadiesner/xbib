package org.xbib.sql.parser.expression.operators.relational;

import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.expression.ExpressionVisitor;


public class IsNullExpression implements Expression {
	private Expression leftExpression;
	private boolean not = false;

	public Expression getLeftExpression() {
		return leftExpression;
	}

	public boolean isNot() {
		return not;
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
        return leftExpression + " IS "+((not)?"NOT ":"")+"NULL";
    }

}
