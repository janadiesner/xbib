package org.xbib.marc.label;

/**
 * Descriptive cataloging form
 *
 * One-character alphanumeric code that indicates characteristics of the descriptive data in the record through reference to cataloging norms. Subfield $e (Description conventions) of field 040 (Cataloging Source) also contains information on the cataloging conventions used.
 *
 * # - Non-ISBD
 * Descriptive portion of the record does not follow International Standard Bibliographic Description (ISBD) cataloging and punctuation provisions.
 *
 * a - AACR 2
 * Descriptive portion of the record is formulated according to the description and punctuation provisions as incorporated into the Anglo-American Cataloging Rules, 2nd Edition (AACR 2) and its manuals.
 *
 * c - ISBD punctuation omitted
 * Descriptive portion of the record contains the punctuation provisions of ISBD, except ISBD punctuation is not present at the end of a subfield.
 *
 * i - ISBD punctuation included
 * Descriptive portion of the record contains the punctuation provisions of ISBD.
 *
 * u - Unknown
 * Institution receiving or sending data in Leader/18 cannot adequately determine the appropriate descriptive cataloging form used in the record. May be used in records converted from another metadata format.
 *
 */
public enum DescriptiveCatalogingForm {

    NON_ISBD(' '),
    AACR2('a'),
    ISBD_PUNCTUATION_OMITTED('c'),
    ISBD_PUNCTUATION_INCLUDED('i'),
    UNKNOWN('u')
    ;

    char ch;
    DescriptiveCatalogingForm(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
