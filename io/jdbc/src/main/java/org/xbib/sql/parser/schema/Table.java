package org.xbib.sql.parser.schema;

import org.xbib.sql.parser.statement.select.FromItem;
import org.xbib.sql.parser.statement.select.FromItemVisitor;
import org.xbib.sql.parser.statement.select.IntoTableVisitor;

public class Table implements FromItem {
	private String schemaName;
	private String name;
	private String alias;
	
	public Table() {
	}

	public Table(String schemaName, String name) {
		this.schemaName = schemaName;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setName(String string) {
		name = string;
	}

	public void setSchemaName(String string) {
		schemaName = string;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String string) {
		alias = string;
	}
	
	public String getWholeTableName() {

		String tableWholeName = null;
		if (name == null) {
			return null;
		}
		if (schemaName != null) {
			tableWholeName = schemaName + "." + name;
		} else {
			tableWholeName = name;
		}
		
		return tableWholeName;

	}

	public void accept(FromItemVisitor fromItemVisitor) {
		fromItemVisitor.visit(this);
	}
	
	public void accept(IntoTableVisitor intoTableVisitor) {
		intoTableVisitor.visit(this);
	}
	
	
	public String toString() {
		return getWholeTableName()+((alias!=null)?" AS "+alias:"");
	}
}
