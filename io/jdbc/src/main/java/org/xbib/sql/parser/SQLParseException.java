
package org.xbib.sql.parser;

public class SQLParseException extends Exception {

	public SQLParseException(Throwable t) {
		super(t);
	}

	public SQLParseException(String msg, Throwable t) {
		super(msg, t);
	}
}
