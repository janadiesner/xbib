package org.xbib.sql.parser.expression.operators.relational;

import org.xbib.sql.parser.statement.select.PlainSelect;

import java.util.List;

public class ExpressionList implements ItemsList {
	private List expressions;

	public ExpressionList() {
	}

	public ExpressionList(List expressions) {
		this.expressions = expressions;
	}

	public List getExpressions() {
		return expressions;
	}

	public void setExpressions(List list) {
		expressions = list;
	}

	public void accept(ItemsListVisitor itemsListVisitor) {
		itemsListVisitor.visit(this);
	}

	public String toString() {
		return PlainSelect.getStringList(expressions, true, true);
	}
}
