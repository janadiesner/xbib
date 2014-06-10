
package org.xbib.sql.parser.statement;

public interface Statement {
	void accept(StatementVisitor visitor);
}
