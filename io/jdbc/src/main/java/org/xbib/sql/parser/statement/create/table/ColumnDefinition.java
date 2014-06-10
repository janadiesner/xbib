package org.xbib.sql.parser.statement.create.table;

import org.xbib.sql.parser.statement.select.PlainSelect;

import java.util.List;

public class ColumnDefinition {
	private String columnName;
	private ColDataType colDataType;
	private List columnSpecStrings;
	public List getColumnSpecStrings() {
		return columnSpecStrings;
	}

	public void setColumnSpecStrings(List list) {
		columnSpecStrings = list;
	}

	public ColDataType getColDataType() {
		return colDataType;
	}

	public void setColDataType(ColDataType type) {
		colDataType = type;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String string) {
		columnName = string;
	}
	
	public String toString() {
		return columnName+" "+colDataType+" "+PlainSelect.getStringList(columnSpecStrings, false, false);
	}

}
