package org.xbib.sql.parser.statement.create.table;

import org.xbib.sql.parser.statement.select.PlainSelect;

import java.util.List;

public class Index {
    private String type;
    private List columnsNames;
    private String name;

    public List getColumnsNames() {
        return columnsNames;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setColumnsNames(List list) {
        columnsNames = list;
    }

    public void setName(String string) {
        name = string;
    }

    public void setType(String string) {
        type = string;
    }

    public String toString() {
        return type + " " + PlainSelect.getStringList(columnsNames, true, true) + (name!=null?" "+name:"");
    }
}