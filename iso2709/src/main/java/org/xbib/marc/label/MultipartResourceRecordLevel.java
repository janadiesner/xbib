package org.xbib.marc.label;

/**
 * Multipart resource record level
 * Record level to which a resource pertains and any record dependencies.
 * This information will facilitate processing the record in different situations.
 * For example, the record may describe a set of items, or it may describe a part of a set.
 * The part may only have a dependent title to be used for identification purposes thus
 * requiring use of additional information to understand its context.
 *
 * # - Not specified or not applicable
 * The distinction between record levels is not specified or not applicable for the type of resource.
 *
 * a - Set
 * Record is for a set consisting of multiple items.
 *
 * b - Part with independent title
 * The record is for a resource which is part of a set and has a title that allows it
 * to be independent of the set record.
 *
 * c - Part with dependent title
 * The record is for a resource which is part of a set but has a title that makes it dependent
 * on the set record to understand its context.
 */
public enum MultipartResourceRecordLevel {

    NOT_SPECIFIED(' '),
    SET('a'),
    PART_WITH_INDEPENDENT_TITLE('b'),
    PART_WITH_DEPENDENT_TITLE('c')
    ;

    char ch;
    MultipartResourceRecordLevel(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
