package org.xbib.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 10957: International Standard Music Number (ISMN)
 *
 * @see <a href="http://www.ismn-international.org/download/Web_ISMN%20Manual_2008-3.pdf">ISMN Manual 2008</a>
 */
public class ISMN implements Comparable<ISMN>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("[\\p{Digit}M\\-]{0,17}");

    private String value;

    private boolean createWithChecksum;

    @Override
    public int compareTo(ISMN ismn) {
        return ismn != null ? normalizedValue().compareTo(ismn.normalizedValue()) : -1;
    }

    @Override
    public ISMN set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public ISMN checksum() {
        this.createWithChecksum = true;
        return this;
    }

    @Override
    public ISMN normalize() {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = value.substring(m.start(), m.end());
            this.value = (value.startsWith("979") ? "" : "979") + dehyphenate(value.replace('M', '0'));
        }
        return this;
    }

    @Override
    public ISMN verify() throws NumberFormatException {
        check();
        return this;
    }

    @Override
    public String normalizedValue() {
        return value;
    }

    @Override
    public String format() {
        return value;
    }

    public GTIN toGTIN() throws NumberFormatException {
        return new GTIN().set(value).normalize().verify();
    }

    private void check() throws NumberFormatException {
        int l = createWithChecksum ? value.length() : value.length() - 1;
        int checksum = 0;
        int weight;
        int val;
        for (int i = 0; i < l; i++) {
            val = value.charAt(i) - '0';
            if (val < 0 || val > 9) {
                throw new NumberFormatException("not a digit: " + val );
            }
            weight = i % 2 == 0 ? 1 : 3;
            checksum += val * weight;
        }
        int chk = 10 - checksum % 10;
        if (createWithChecksum) {
            char ch = (char)('0' + chk);
            value = value + ch;
        }
        boolean valid = chk == value.charAt(l) - '0';
        if (!valid) {
            throw new NumberFormatException("invalid checksum: " + chk + " != " + value.charAt(l));
        }
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
