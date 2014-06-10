
package org.xbib.sql.parser.expression;

import java.sql.Date;

public class DateValue implements Expression {
	private Date value;

	public DateValue(String value) {
		this.value = Date.valueOf(value.substring(1, value.length() - 1));
	}
	
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public Date getValue() {
		return value;
	}

	public void setValue(Date d) {
		value = d;
	}


}
