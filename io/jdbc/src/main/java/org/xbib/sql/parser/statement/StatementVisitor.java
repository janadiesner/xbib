
package org.xbib.sql.parser.statement;

import org.xbib.sql.parser.statement.create.table.CreateTable;
import org.xbib.sql.parser.statement.delete.Delete;
import org.xbib.sql.parser.statement.drop.Drop;
import org.xbib.sql.parser.statement.insert.Insert;
import org.xbib.sql.parser.statement.replace.Replace;
import org.xbib.sql.parser.statement.select.Select;
import org.xbib.sql.parser.statement.truncate.Truncate;
import org.xbib.sql.parser.statement.update.Update;

public interface StatementVisitor {

    void visit(Select select);

    void visit(Delete delete);

    void visit(Update update);

    void visit(Insert insert);

    void visit(Replace replace);

    void visit(Drop drop);

    void visit(Truncate truncate);

    void visit(CreateTable createTable);

}
