
package org.xbib.sql.parser.expression;

import org.xbib.sql.parser.statement.select.PlainSelect;

import java.util.List;

public class CaseExpression implements Expression {

	private Expression switchExpression;
	
	private List whenClauses;
	
	private Expression elseExpression;

	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}
	
	public Expression getSwitchExpression() {
		return switchExpression;
	}

	public void setSwitchExpression(Expression switchExpression) {
		this.switchExpression = switchExpression;
	}
	
	public Expression getElseExpression() {
		return elseExpression;
	}
	public void setElseExpression(Expression elseExpression) {
		this.elseExpression = elseExpression;
	}
	public List getWhenClauses() {
		return whenClauses;
	}
	
	public void setWhenClauses(List whenClauses) {
		this.whenClauses = whenClauses;
	}
	
	public String toString() {
		return "CASE "+((switchExpression!=null)?switchExpression+" ":"")+
				PlainSelect.getStringList(whenClauses,false, false)+" "+
				((elseExpression!=null)?"ELSE "+elseExpression+" ":"")+
				"END";
	}
}
