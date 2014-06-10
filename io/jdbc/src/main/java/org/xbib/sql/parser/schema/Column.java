
package org.xbib.sql.parser.schema;

import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.expression.ExpressionVisitor;

public class Column implements Expression {
	private String columnName = "";
	private Table table;
	
	public Column() {
	}

	public Column(Table table, String columnName) {
		this.table = table;
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public Table getTable() {
		return table;
	}

	public void setColumnName(String string) {
		columnName = string;
	}

	public void setTable(Table table) {
		this.table = table;
	}
	
	public String getWholeColumnName() {
		
		String columnWholeName = null;
		String tableWholeName = table.getWholeTableName();
		
		if (tableWholeName != null && tableWholeName.length() != 0) {
			columnWholeName = tableWholeName + "." + columnName;
		} else {
			columnWholeName = columnName;
		}
		
		return columnWholeName;

	}
	
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public String toString() {
		return getWholeColumnName();
	}
}
