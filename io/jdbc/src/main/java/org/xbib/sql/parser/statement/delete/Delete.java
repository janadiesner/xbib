package org.xbib.sql.parser.statement.delete;

import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.schema.Table;
import org.xbib.sql.parser.statement.Statement;
import org.xbib.sql.parser.statement.StatementVisitor;

public class Delete implements Statement {
	private Table table;
	private Expression where;
	
	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	public Table getTable() {
		return table;
	}

	public Expression getWhere() {
		return where;
	}

	public void setTable(Table name) {
		table = name;
	}

	public void setWhere(Expression expression) {
		where = expression;
	}

	public String toString() {
		return "DELETE FROM "+table+((where!=null)?" WHERE "+where:"");
	}
}
