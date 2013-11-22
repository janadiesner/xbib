package org.xbib.standardnumber;

/**
 * A standard number is a number that
 *
 * - is backed by an international standard or a de-facto community use
 *
 * - can accept alphanumeric values (digits and letters and separator characters)
 *
 * - can be normalized
 *
 * - can be verified and raise en error is verification fails
 *
 * - must have a checksum
 *
 * - can be formatted to a printable representation
 *
 */
public interface StandardNumber {

    StandardNumber set(String value);

    StandardNumber checksum();

    StandardNumber normalize();

    StandardNumber verify() throws NumberFormatException;

    String normalized();

    String format();
}
