package org.xbib.sql.parser.deparser;

import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.expression.ExpressionVisitor;
import org.xbib.sql.parser.expression.operators.relational.ExpressionList;
import org.xbib.sql.parser.expression.operators.relational.ItemsListVisitor;
import org.xbib.sql.parser.schema.Column;
import org.xbib.sql.parser.statement.replace.Replace;
import org.xbib.sql.parser.statement.select.SelectVisitor;
import org.xbib.sql.parser.statement.select.SubSelect;

import java.util.Iterator;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string)
 * a {@link org.xbib.sql.parser.statement.replace.Replace}
 */
public class ReplaceDeParser implements ItemsListVisitor {
	protected StringBuffer buffer;
	protected ExpressionVisitor expressionVisitor;
	protected SelectVisitor selectVisitor;

	public ReplaceDeParser() {
	}

	/**
	 * @param expressionVisitor a {@link ExpressionVisitor} to de-parse expressions. It has to share the same<br>
	 * StringBuffer (buffer parameter) as this object in order to work
	 * @param selectVisitor a {@link SelectVisitor} to de-parse {@link org.xbib.sql.parser.statement.select.Select}s.
	 * It has to share the same<br>
	 * StringBuffer (buffer parameter) as this object in order to work
	 * @param buffer the buffer that will be filled with the select
	 */
	public ReplaceDeParser(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuffer buffer) {
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

	public void deParse(Replace replace) {
		buffer.append("REPLACE " + replace.getTable().getWholeTableName());
		if (replace.getItemsList() != null) {
			if (replace.getColumns() != null) {
				buffer.append(" (");
				for (int i = 0; i < replace.getColumns().size(); i++) {
					Column column = (Column) replace.getColumns().get(i);
					buffer.append(column.getWholeColumnName());
					if (i < replace.getColumns().size() - 1) {
						buffer.append(", ");
					}
				}
				buffer.append(") ");
			} else {
				buffer.append(" ");
			}

		} else {
			buffer.append(" SET ");
			for (int i = 0; i < replace.getColumns().size(); i++) {
				Column column = (Column) replace.getColumns().get(i);
				buffer.append(column.getWholeColumnName() + "=");

				Expression expression = (Expression) replace.getExpressions().get(i);
				expression.accept(expressionVisitor);
				if (i < replace.getColumns().size() - 1) {
					buffer.append(", ");
				}

			}
		}

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
