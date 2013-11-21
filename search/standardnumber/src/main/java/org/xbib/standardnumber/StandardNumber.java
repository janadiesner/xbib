package org.xbib.standardnumber;

public interface StandardNumber {

    StandardNumber setValue(String value);

    StandardNumber checksum();

    StandardNumber parse();

    StandardNumber verify() throws NumberFormatException;

    String getValue();

    String format();
}
