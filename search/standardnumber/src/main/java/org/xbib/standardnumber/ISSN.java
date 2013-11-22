
package org.xbib.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The International Standard Serial Number (ISSN) is a unique
 * eight-digit number used to identify a print or electronic periodical
 * publication. The ISSN system was adopted as international standard
 * ISO 3297 in 1975. The ISO subcommittee TC 46/SC 9 is responsible
 * for the standard.
 *
 * Quoted from http://www.issn.org/2-22636-All-about-ISSN.php
 *
 * The ISSN (International Standard Serial Number) is an eight-digit number
 * which identifies periodical publications as such, including electronic
 * serials.
 *
 * The ISSN is a numeric code which is used as an identifier: it has no
 * signification in itself and does not contain in itself any information
 * referring to the origin or contents of the publication.
 *
 * The ISSN takes the form of the acronym ISSN followed by two groups
 * of four digits, separated by a hyphen. The eighth character is a
 * control digit calculated according to a modulo 11 algorithm on
 * the basis of the 7 preceding digits; this eighth control character
 * may be an "X" if the result of the computing is equal to "10",
 * in order to avoid any ambiguity.
 *
 *  The ISSN is linked to a standardized form of the title of the
 *  identified serial, known as the "key title", which repeats
 *  the title of the publication, qualifying it with additional elements
 *  in order to distinguish it from other publications having identical
 *  titles.
 *
 *  If the title of the publication changes in any significant way,
 *  a new ISSN must be assigned in order to correspond to this new form
 *  of title and avoid any confusion. A serial publication whose
 *  title is modified several times in the course of its existence
 *  will be assigned each time a new ISSN, thus allowing precise
 *  identification of each form of the title : in fact it is then
 *  considered that they are different publications even if there
 *  is a logical link between them.
 *
 *  Contrary to other types of publications, the world of serial
 *  publications is particularly changeable and complex :
 *  the lifetime of a title may be extremely short; many publications
 *  may be part of a complex set of relationships, etc.
 *  These particularities themselves necessitated the introduction
 *  of the ISSN.
 *
 */
public class ISSN implements Comparable<ISSN>, StandardNumber {

    private final static Pattern PATTERN = Pattern.compile("[\\p{Digit}xX\\-]+");

    private String value;

    private String formatted;

    private boolean valid;

    private boolean createWithChecksum;

    @Override
    public ISSN set(String value) {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = dehyphenate(value.substring(m.start(), m.end()));
        }
        return this;
    }

    @Override
    public ISSN checksum() {
        this.createWithChecksum = true;
        return this;
    }

    @Override
    public int compareTo(ISSN issn) {
        return value != null ? value.compareTo((issn).normalized()) : -1;
    }

    public ISSN normalize() {
        this.value = dehyphenate(value);
        return this;
    }

    @Override
    public ISSN verify() throws NumberFormatException {
        check();
        return this;
    }

    /**
     * Returns the value representation of the standard number
     * @return value
     */
    @Override
    public String normalized() {
        return value;
    }

    /**
     * Format this number
     *
     * @return the formatted number
     */
    public String format() {
        if (formatted == null) {
            if (!valid) {
                return value;
            }
            this.formatted = value.substring(0, 4) + "-" + value.substring(4, 8);
        }
        return formatted;
    }

    private void check() throws NumberFormatException {
        int l = createWithChecksum ? value.length() : value.length() - 1;
        int checksum = 0;
        int weight;
        int val;
        for (int i = 0; i < l; i++) {
            val = value.charAt(i) - '0';
            if (val < 0 || val > 9) {
                throw new IllegalArgumentException("not a digit in " + value);
            }
            weight = 8 - i;
            checksum += weight * val;
        }
        int chk = checksum % 11;
        char p = chk == 0 ? '0' : chk == 1 ? 'X' : (char)((11-chk) + '0');
        /*int mod = sum % 11;
        mod = mod == 0 ? 0 : 11 - mod; 
        char p = mod == 10 ? 'X' : (char) ('0' + mod);
        */
        this.valid = p == Character.toUpperCase(value.charAt(l));
        if (!valid) {
            throw new NumberFormatException("invalid checksum: " + chk + " != " + value.charAt(l));
        }
    }

    public GTIN toGTIN() throws NumberFormatException {
        return new GTIN().set("977" + value.substring(0, 7) + "000").checksum().normalize().verify();
    }

    public GTIN toGTIN(String additionalCode) throws NumberFormatException {
        // "977" + ISSN + add-on + placeholder for checksum
        return new GTIN().set("977" + value.substring(0, 7) + additionalCode + "0").checksum().normalize().verify();
    }

    private String dehyphenate(String isbn) {
        StringBuilder sb = new StringBuilder(isbn);
        int i = sb.indexOf("-");
        while (i >= 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf("-");
        }
        return sb.toString();
    }

}
