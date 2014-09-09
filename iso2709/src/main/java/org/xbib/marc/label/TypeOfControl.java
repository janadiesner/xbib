package org.xbib.marc.label;

/**
 * Type of control
 *
 * # - No specified type
 * No type applies to the item being described.
 *
 * a - Archival
 * Material is described according to archival descriptive rules, which focus on the contextual
 * relationships between items and on their provenance rather than on bibliographic detail.
 * The specific set of rules for description may be found in field 040, subfield $e.
 * All forms of material can be controlled archivally.
 */
public enum TypeOfControl {

    NO_SPECIFIED_TYPE(' '),
    ARCHIVAL('a')
    ;

    char ch;
    TypeOfControl(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
