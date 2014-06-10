package org.xbib.sql.parser.statement.insert;

import org.xbib.sql.parser.expression.operators.relational.ItemsList;
import org.xbib.sql.parser.schema.Table;
import org.xbib.sql.parser.statement.Statement;
import org.xbib.sql.parser.statement.StatementVisitor;
import org.xbib.sql.parser.statement.select.PlainSelect;

import java.util.List;


public class Insert implements Statement {
	private Table table;
	private List columns;
	private ItemsList itemsList;
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

	public void setColumns(List list) {
		columns = list;
	}

	public ItemsList getItemsList() {
		return itemsList;
	}

	public void setItemsList(ItemsList list) {
		itemsList = list;
	}

    public boolean isUseValues() {
        return useValues;
    }
    
    public void setUseValues(boolean useValues) {
        this.useValues = useValues;
    }
    
	public String toString() {
		String sql = "";

		sql = "INSERT INTO ";
		sql += table+" ";
		sql += ((columns!=null)?PlainSelect.getStringList(columns, true, true)+" ":"");
		
		if(useValues) {
			sql += "VALUES "+itemsList+"";
		} else {
			sql += ""+itemsList+"";
		}
		
		return sql;
	}

}
