
package org.xbib.sql.parser.statement.update;

import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.schema.Table;
import org.xbib.sql.parser.statement.Statement;
import org.xbib.sql.parser.statement.StatementVisitor;

import java.util.List;

/**
 * The update statement.
 */
public class Update implements Statement {
	private Table table;
	private Expression where;
	private List columns;
	private List expressions;

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

	/**
	 * The {@link org.xbib.sql.parser.schema.Column}s in this update (as col1 and col2 in UPDATE col1='a', col2='b')
	 * @return a list of {@link org.xbib.sql.parser.schema.Column}s
	 */
	public List getColumns() {
		return columns;
	}

	/**
	 * The {@link Expression}s in this update (as 'a' and 'b' in UPDATE col1='a', col2='b')
	 * @return a list of {@link Expression}s
	 */
	public List getExpressions() {
		return expressions;
	}

	public void setColumns(List list) {
		columns = list;
	}

	public void setExpressions(List list) {
		expressions = list;
	}

}
