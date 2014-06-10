
package org.xbib.sql.parser.expression.operators.arithmetic;

import org.xbib.sql.parser.expression.BinaryExpression;
import org.xbib.sql.parser.expression.ExpressionVisitor;


public class BitwiseXor extends BinaryExpression {
	
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}
	
	public String getStringExpression() {
		return "^";
	}
}
