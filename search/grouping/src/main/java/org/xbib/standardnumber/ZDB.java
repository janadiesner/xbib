/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.standardnumber;

/**
 * Zeitschruftendatenbank
 *
 */
public class ZDB implements StandardNumber {

    private String value;

    private boolean isValid;

    public ZDB(String value)
            throws InvalidStandardNumberException {
        parse(dehyphenate(value));
    }

    public String getValue() {
        return value;
    }

    public String getOriginal() {
        return value;
    }

    @Override
    public String getAcronym() {
        return "ZDB";
    }


    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public String getStandardNumberValue() throws InvalidStandardNumberException {
        return isValid ? value : null;
    }

    @Override
    public String getStandardNumberPrintableRepresentation() throws InvalidStandardNumberException {
        return getStandardNumberValue();
    }

    @Override
    public int compareTo(Object o) {
        int i = -1;
        if (value != null) {
            i = value.compareTo(((ZDB) o).getValue());
        }
        return i;
    }

    /**
     * Parse input and recognize
     *
     * @param value the input string
     * @throws InvalidStandardNumberException
     */
    private void parse(String value) throws InvalidStandardNumberException {
        this.isValid = false;
        if (check(value)) {
            this.value = value;
            this.isValid = true;
        } else {
            throw new InvalidStandardNumberException("bad checksum");
        }
    }

    /**
     * Check if ZDB is valid
     *
     * @param value the ZDB
     * @return the check digit
     *
     * @throws InvalidStandardNumberException
     */
    private boolean check(String value) throws InvalidStandardNumberException {
        char[] theChars = value.toCharArray();
        int l = theChars.length - 1;
        if (theChars[l] == 'X' || theChars[l] == 'x') {
            theChars[l] = 10 + '0';
        }
        int checksum = 0;
        int weight = 2;
        int val;
        for (int i = l-1; i >= 0; i--) {
            val = theChars[i] - '0';
            if (val < 0 || val > 10) {
                throw new InvalidStandardNumberException("not a digit: " + val );
            }
            checksum += val * weight++;
        }
        return theChars[l] - '0' == checksum % 11;
    }

    private String dehyphenate(String value) {
        StringBuilder sb = new StringBuilder(value);
        int i = sb.indexOf("-");
        while (i >= 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf("-");
        }
        return sb.toString();
    }
}
