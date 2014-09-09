package org.xbib.marc.label;

/**
 * Character encoding
 *
 * Identifies the character encoding used in the record.
 *
 * The encoding affects the number of octets needed per character, the placement of
 * non-spacing characters, the use of escape sequences, and may affect the character
 * repertoire.
 *
 * # - MARC-8
 * Character coding in the record uses the 8-bit character sets described in MARC 21 Specifications
 * for Record Structure, Character Sets, and Exchange Media. Non-default character sets used are
 * identified in field 066.
 *
 * a - UCS/Unicode
 * Character coding in the record makes use of characters from the Universal Coded Character Set (UCS)
 * (ISO 10646), or Unicode, an industry subset.
 *
 *
 */
public enum Encoding {

    MARC8(' '),
    UCS_UNICODE('a');

    char ch;
    Encoding(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
