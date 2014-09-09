package org.xbib.marc.label;

/**
 *
 *  Record status
 *
 *  One-character alphabetic code that indicates the relationship of the record to a file
 *  for file maintenance purposes.
 *
 *  a - Increase in encoding level
 *  Encoding level (Leader/17) of the record has been changed to a higher encoding level.
 *
 *  Indicates an increase in the level of cataloging (e.g., code a is used when a preliminary
 *  cataloging record (code 5 in Leader/17) is raised to full cataloging level (code # in Leader/17)).
 *
 *  c - Corrected or revised
 *  Addition/change other than in the Encoding level code has been made to the record.
 *
 *  d - Deleted
 *  Record has been deleted.
 *
 *  n - New
 *  Record is newly input.
 *
 *  p - Increase in encoding level from prepublication
 *  Prepublication record has had a change in cataloging level resulting from the availability
 *  of the published item.
 *
 *  Example: a CIP record (code 8 in Leader/17)) upgraded to a full record (code # or 1 in Leader/17.)
 */
public enum RecordStatus {

    INCREASE_IN_ENCODING_LEVEL('a'),
    CORRECTED_OR_REVISED('c'),
    DELETED('d'),
    NEW('n'),
    INCREASE_IN_ENCODING_LEVEL_FROM_PREPUBLICATION('p')
    ;

    char ch;
    RecordStatus(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
