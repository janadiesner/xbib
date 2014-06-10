package org.xbib.sql.parser.deparser;

import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.expression.ExpressionVisitor;
import org.xbib.sql.parser.schema.Column;
import org.xbib.sql.parser.statement.update.Update;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string)
 * an {@link org.xbib.sql.parser.statement.update.Update}
 */
public class UpdateDeParser {
	protected StringBuffer buffer;
	protected ExpressionVisitor expressionVisitor;
	
	public UpdateDeParser() {
	}
	
	/**
	 * @param expressionVisitor a {@link ExpressionVisitor} to de-parse expressions. It has to share the same<br>
	 * StringBuffer (buffer parameter) as this object in order to work
	 * @param buffer the buffer that will be filled with the select
	 */
	public UpdateDeParser(ExpressionVisitor expressionVisitor, StringBuffer buffer) {
		this.buffer = buffer;
		this.expressionVisitor = expressionVisitor;
	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

	public void deParse(Update update) {
		buffer.append("UPDATE " + update.getTable().getWholeTableName() + " SET ");
		for (int i = 0; i < update.getColumns().size(); i++) {
			Column column = (Column) update.getColumns().get(i);
			buffer.append(column.getWholeColumnName() + "=");

			Expression expression = (Expression) update.getExpressions().get(i);
			expression.accept(expressionVisitor);
			if (i < update.getColumns().size() - 1) {
				buffer.append(", ");
			}

		}
		
		if (update.getWhere() != null) {
			buffer.append(" WHERE ");
			update.getWhere().accept(expressionVisitor);
		}

	}

	public ExpressionVisitor getExpressionVisitor() {
		return expressionVisitor;
	}

	public void setExpressionVisitor(ExpressionVisitor visitor) {
		expressionVisitor = visitor;
	}

}
