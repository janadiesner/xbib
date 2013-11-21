package org.xbib.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UPC implements Comparable<UPC>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("[\\p{Digit}]{0,12}");

    private String value;

    private boolean createWithChecksum;

    @Override
    public int compareTo(UPC upc) {
        return upc != null ? getValue().compareTo(upc.getValue()) : -1;
    }

    @Override
    public UPC setValue(String value) {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = value.substring(m.start(), m.end());
        }
        return this;
    }

    @Override
    public UPC checksum() {
        this.createWithChecksum = true;
        return this;
    }

    @Override
    public UPC parse() {
        return this;
    }

    @Override
    public UPC verify() throws NumberFormatException {
        check();
        return this;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String format() {
        return value;
    }

    private void check() throws NumberFormatException {
        int l = value.length() - 1;
        int checksum = 0;
        int weight;
        int val;
        for (int i = 0; i < l; i++) {
            val = value.charAt(i) - '0';
            if (val < 0 || val > 9) {
                throw new NumberFormatException("not a digit: " + val );
            }
            weight = i%2 == 0 ? 3 : 1;
            checksum += val * weight;
        }
        int chk = 10 - checksum % 10;
        if (createWithChecksum) {
            char ch = (char)('0' + chk);
            value = value.substring(0, l) + ch;
        }
        boolean valid = chk == (value.charAt(l) - '0');
        if (!valid) {
            throw new NumberFormatException("invalid checksum: " + chk + " != " + value.charAt(l));
        }
    }
}
