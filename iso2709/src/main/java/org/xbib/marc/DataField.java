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
package org.xbib.marc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * An ISO 2709 data field, consisting of a list of subfields
 */
public class DataField extends ArrayList<Field> {

    public final static DataField FORMAT_KEY = new DataField("FORMAT");

    public final static DataField TYPE_KEY = new DataField("TYPE");

    public final static DataField LEADER_KEY = new DataField("LEADER");

    public DataField() {
        super();
    }

    private DataField(String tag) {
        super();
        super.add(new Field(tag));
    }

    /**
     * Build a pattern of this field collection for matching
     * @param map the map
     */
    public void makePattern(Map<String, String[]> map) {
        StringBuilder pattern = new StringBuilder();
        // walk through sorted designators
        String tag = null;
        String[] ind = null;
        String sub = null;
        if (isEmpty()) {
            return;
        }
        for (Field field : this) {
            if (tag == null) {
                tag = field.tag();
            }
            int l = field.indicator() != null ? field.indicator().length() : 0;
            if (ind == null && l > 0) {
                ind = new String[field.indicator().length()];
                for (int i = 0; i < l; i++) {
                    ind[i] = field.indicator().substring(i, i + 1);
                }
            }
            if (sub == null) {
                sub = field.subfieldId();
            }
            if (tag != null && !tag.equals(field.tag())) {
                // unequal tags are very unlikely when parsing MARC
                switchToNextTag(map, pattern, tag, ind, sub);
                tag = field.tag();
                ind = null;
                sub = null;
            } else {
                // new indicator?
                if (ind != null) {
                    for (int i = 0; i < l; i++) {
                        char ch = field.indicator().charAt(i);
                        int pos = ind[i].indexOf(ch);
                        if (pos < 0) {
                            ind[i] = ind[i] + ch;
                        }
                    }
                }
                // new subfield id?
                if (sub != null && field.subfieldId() != null && !sub.contains(field.subfieldId())) {
                    sub = sub + field.subfieldId();
                }
            }
        }
        // last tag
        if (tag != null) {
            switchToNextTag(map, pattern, tag, ind, sub);
        }
    }

    private void switchToNextTag(Map<String, String[]> map,
                                 StringBuilder pattern, String tag, String[] ind, String sub) {
        if (pattern.length() > 0) {
            pattern.append('|');
        }
        pattern.append(tag);
        String p = pattern.toString();
        // merge with pattern map, if any
        if (map != null) {
            int l = ind != null ? ind.length : 0;
            String[] v = new String[l+1];
            if (ind != null) {
                System.arraycopy(ind, 0, v, 0, l);
            }
            if (sub != null) {
                v[l] = sub;
            }
            if (!map.containsKey(p)) {
                map.put(p, v);
            } else {
                // melt
                String[] s = map.get(p);
                if (s != null) {
                    // melt indicators
                    if (ind != null) {
                        for (int i = 0; i < l; i++) {
                            if (!s[i].contains(ind[i])) {
                                s[i] += ind[i];
                            }
                        }
                    }
                    // melt subfield
                    if (sub != null) {
                        for (int i = 0; i < sub.length(); i++) {
                            if (s[l].indexOf(sub.charAt(i)) < 0) {
                                s[l] += sub.charAt(i);
                            }
                        }
                    }
                    map.put(p,s);
                } else {
                    map.put(p,v);
                }
            }
        }
    }

    public String toSpec() {
        // a better (faster) method would be insert sort
        Map<String,String[]> m = new TreeMap<String,String[]>();
        makePattern(m);
        StringBuilder sb = new StringBuilder();
        for (String k : m.keySet()) {
            sb.append(k);
            String[] values = m.get(k);
            if (values != null) {
                for (String v : values) {
                    sb.append('$');
                    if (v != null) {
                        // sort characters may be slow
                        char[] ch = v.toCharArray();
                        Arrays.sort(ch);
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }

    public Field getFirst() {
        return get(0);
    }

    public Field removeFirst() {
        return remove(0);
    }

    public Field getLast() {
        return isEmpty() ? null : get(size()-1);
    }

    public Field removeLast() {
        return remove(size()-1);
    }

    public void addToFieldCollection(Field field) {
        // insert sort. Search first occurence where the field tag is higher.
        if (isEmpty()) {
            add(0, field);
        } else {
            boolean inserted = false;
            for (int i = 0; i < size(); i++) {
                Field f = get(i);
                if (f.compareTo(field) > 0) {
                    add(i, field);
                    inserted = true;
                    break;
                }
            }
            if (!inserted) {
                add(field);
            }
        }
    }

    public String getString() {
        return super.toString();
    }

    public String toString() {
        return toSpec();
    }

}
