package org.xbib.sql.parser.statement.truncate;

import org.xbib.sql.parser.schema.Table;
import org.xbib.sql.parser.statement.Statement;
import org.xbib.sql.parser.statement.StatementVisitor;

/**
 * A TRUNCATE TABLE statement
 */
public class Truncate implements Statement {
	private Table table;

	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public String toString() {
		return "TRUNCATE TABLE "+table;
	}
}
