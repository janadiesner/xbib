package org.xbib.sql.parser.statement.select;

public class AllColumns implements SelectItem {
	public void accept(SelectItemVisitor selectItemVisitor) {
		selectItemVisitor.visit(this);
	}
	
	public String toString() {
		return "*";
	}
}
