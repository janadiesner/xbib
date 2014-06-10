package org.xbib.sql.parser.statement.replace;

import org.xbib.sql.parser.expression.operators.relational.ItemsList;
import org.xbib.sql.parser.schema.Table;
import org.xbib.sql.parser.statement.Statement;
import org.xbib.sql.parser.statement.StatementVisitor;
import org.xbib.sql.parser.statement.select.PlainSelect;

import java.util.List;

public class Replace implements Statement {
	private Table table;
	private List columns;
	private ItemsList itemsList;
	private List expressions;
	private boolean useValues = true;

	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table name) {
		table = name;
	}

	public List getColumns() {
		return columns;
	}

	public ItemsList getItemsList() {
		return itemsList;
	}

	public void setColumns(List list) {
		columns = list;
	}

	public void setItemsList(ItemsList list) {
		itemsList = list;
	}

	public List getExpressions() {
		return expressions;
	}

	public void setExpressions(List list) {
		expressions = list;
	}

	public boolean isUseValues() {
        return useValues;
    }
    
	public void setUseValues(boolean useValues) {
        this.useValues = useValues;
    }
	
	public String toString() {
		String sql = "REPLACE "+table;
		
		if(expressions != null && columns != null ) {
			sql += " SET ";
			for (int i = 0, s = columns.size(); i < s; i++) {
				sql += ""+columns.get(i)+"="+expressions.get(i);
				sql += (i<s-1)?", ":"";
			}
		}
		else if( columns != null ) {
			sql += " "+PlainSelect.getStringList(columns, true, true);
		}
		
		if( itemsList != null ) {
			if( useValues ) {
				sql += " VALUES";
			}
			sql += " "+itemsList;
		}
		
		return sql;
	}

}
