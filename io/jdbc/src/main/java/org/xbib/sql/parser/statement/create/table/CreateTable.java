package org.xbib.sql.parser.statement.create.table;

import org.xbib.sql.parser.schema.Table;
import org.xbib.sql.parser.statement.Statement;
import org.xbib.sql.parser.statement.StatementVisitor;
import org.xbib.sql.parser.statement.select.PlainSelect;

import java.util.List;

public class CreateTable implements Statement {

    private Table table;
    private List tableOptionsStrings;
    private List columnDefinitions;
    private List indexes;

    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColumnDefinitions(List list) {
        columnDefinitions = list;
    }

    public List getTableOptionsStrings() {
        return tableOptionsStrings;
    }

    public void setTableOptionsStrings(List list) {
        tableOptionsStrings = list;
    }

    public List getIndexes() {
        return indexes;
    }

    public void setIndexes(List list) {
        indexes = list;
    }

    public String toString() {
        String sql = "";

        sql = "CREATE TABLE " + table + " (";

        sql += PlainSelect.getStringList(columnDefinitions, true, false);
        if (indexes != null && indexes.size() != 0) {
            sql += ", ";
            sql += PlainSelect.getStringList(indexes);
        }
        sql += ") ";
        sql += PlainSelect.getStringList(tableOptionsStrings, false, false);

        return sql;
    }
}