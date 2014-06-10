package org.xbib.sql.parser.deparser;

import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.expression.ExpressionVisitor;
import org.xbib.sql.parser.expression.operators.relational.ExpressionList;
import org.xbib.sql.parser.expression.operators.relational.ItemsListVisitor;
import org.xbib.sql.parser.schema.Column;
import org.xbib.sql.parser.statement.insert.Insert;
import org.xbib.sql.parser.statement.select.SelectVisitor;
import org.xbib.sql.parser.statement.select.SubSelect;

import java.util.Iterator;


/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string)
 * an {@link org.xbib.sql.parser.statement.insert.Insert}
 */
public class InsertDeParser implements ItemsListVisitor {
	protected StringBuffer buffer;
	protected ExpressionVisitor expressionVisitor;
	protected SelectVisitor selectVisitor;

	public InsertDeParser() {
	}

	/**
	 * @param expressionVisitor a {@link org.xbib.sql.parser.expression.ExpressionVisitor} to de-parse {@link org.xbib.sql.parser.expression.Expression}s. It has to share the same<br>
	 * StringBuffer (buffer parameter) as this object in order to work
	 * @param selectVisitor a {@link SelectVisitor} to de-parse {@link org.xbib.sql.parser.statement.select.Select}s.
	 * It has to share the same<br>
	 * StringBuffer (buffer parameter) as this object in order to work
	 * @param buffer the buffer that will be filled with the insert
	 */
	public InsertDeParser(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuffer buffer) {
		this.buffer = buffer;
		this.expressionVisitor = expressionVisitor;
		this.selectVisitor = selectVisitor;
	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

	public void deParse(Insert insert) {
		buffer.append("INSERT INTO ");
		buffer.append(insert.getTable().getWholeTableName());
		if (insert.getColumns() != null) {
			buffer.append("(");
			for (Iterator iter = insert.getColumns().iterator(); iter.hasNext();) {
				Column column = (Column) iter.next();
				buffer.append(column.getColumnName());
				if (iter.hasNext()) {
					buffer.append(", ");
				}
			}
			buffer.append(")");
		}

		insert.getItemsList().accept(this);

	}

	public void visit(ExpressionList expressionList) {
		buffer.append(" VALUES (");
		for (Iterator iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
			Expression expression = (Expression) iter.next();
			expression.accept(expressionVisitor);
			if (iter.hasNext())
				buffer.append(", ");
		}
		buffer.append(")");
	}
	
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(selectVisitor);
	}
	public ExpressionVisitor getExpressionVisitor() {
		return expressionVisitor;
	}

	public SelectVisitor getSelectVisitor() {
		return selectVisitor;
	}

	public void setExpressionVisitor(ExpressionVisitor visitor) {
		expressionVisitor = visitor;
	}

	public void setSelectVisitor(SelectVisitor visitor) {
		selectVisitor = visitor;
	}

}
