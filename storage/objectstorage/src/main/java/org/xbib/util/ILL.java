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
package org.xbib.util;

import org.xbib.standardnumber.StandardNumber;
import org.xbib.standardnumber.check.DihedralGroup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ILL implements Comparable<ILL>, StandardNumber {

    private final static Pattern ILL_PATTERN = Pattern.compile("[\\p{Alnum}\\-]+");

    private final static DihedralGroup verhoeff = new DihedralGroup();

    private String value;

    private String formatted;

    private String isilPrefix;

    private String isilBody;

    private int year;

    private int number;

    private int counter;

    @Override
    public ILL set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public int compareTo(ILL obj) {
        return value != null ? value.compareTo((obj.normalizedValue())) : -1;
    }

    @Override
    public StandardNumber normalize() {
        Matcher m = ILL_PATTERN.matcher(value);
        if (m.find()) {
            this.value = value.substring(m.start(), m.end());
        }
        return this;
    }

    @Override
    public StandardNumber verify() throws NumberFormatException {
        check();
        return this;
    }

    @Override
    public String normalizedValue() {
        return value;
    }

    @Override
    public String format() {
        if (formatted == null) {
            this.formatted = value;
        }
        return formatted;
    }

    @Override
    public ILL checksum() {
        return this;
    }

    public String getISIL() {
        return isilPrefix + "-" + isilBody;
    }

    public int getYear() {
        return year;
    }

    public int getNumber() {
        return number;
    }

    public int getCounter() {
        return counter;
    }

    private void check() throws NumberFormatException {
        // segmentize
        String[] seg = value.split("-");
        if (seg.length != 6) {
            throw new NumberFormatException("not a valid number");
        }
        this.isilPrefix = seg[0];
        this.isilBody = seg[1];
        this.year = Integer.parseInt(seg[2]);
        this.number = Integer.parseInt(seg[3]);
        this.counter = Integer.parseInt(seg[4]);
        int check = Integer.parseInt(seg[5].substring(0,1));
        int v = verhoeff.compute(seg[3]);
        if (check != v) {
            throw new NumberFormatException("invalid checksum: number=" + seg[3] + " check=" + check + " computed=" + v);
        }
    }
}
