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
package org.xbib.elements.marc.dialects.mab;

import org.xbib.elements.AbstractSpecification;
import org.xbib.elements.Element;
import org.xbib.elements.marc.MARCSpecification;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MAB specification for field collection descriptions.
 *
 * There are specific fields with periodic specifications.
 *
 * 100-199 Personenname, Periode 4, Anzahl 25
 * 200-299 Körperschaftsname, Periode 4, Anzahl 25
 * 340-355 Parallelsachtitel in Ansetzungsform, Periode 4, Anzahl 4
 * 410-419 Verlag, Periode 5, Anzahl 2
 * 451-500 Gesamttitel, Periode 10, Anzahl 5
 * 621-632 Gesamttitel Sekundärform, Periode 6, Anzahl 2
 * 800-829 NE Periode 6, Anzahl 5
 * 900-949 SW-Ketten Periode 5, Anzahl 10
 */
public class MABSpecification extends MARCSpecification {

    private Periodic[] periodicfields = new Periodic[] {
            new Periodic(100, 4, 25),
            new Periodic(200, 4, 25),
            new Periodic(340, 4, 4),
            new Periodic(410, 5, 2),
            new Periodic(451, 10, 5),
            new Periodic(621, 6, 2),
            new Periodic(800, 6, 5),
            new Periodic(900, 5, 10),
            new Periodic(950, 5, 10, -50) // custom subject forms in 950-999 should be treated like 900-949
    };

    public Map addSpec(String value, Element element, Map map) {
        this.value = value;
        int pos = value.indexOf('$');
        String h = pos > 0 ? value.substring(0,pos) : null;
        String t = pos > 0 ? value.substring(pos+1) : value;
        if (h == null) {
            addSpec(null, t, element, map);
            return map;
        }
        // Periodic fields are expanded here. Just the base fields needs to be specified.
        char[] ch = h.toCharArray();
        if (ch.length == 3 && ch[0] >= '0' && ch[0] <= '9' && ch[1] >= '0' && ch[1] <= '9' && ch[2] >= '0' && ch[2] <= '9') {
            Integer i = Integer.parseInt(h); // safe!
            for (Periodic p : periodicfields) {
                if (p.inPeriod(i)) {
                    List<Integer> list = p.spanPeriods(i);
                    for (Integer j : list) {
                        addSpec(Integer.toString(j), t, element, map);
                    }
                }
            }
        } else {
            addSpec(h, t, element, map);
        }
        return map;
    }


    /*public Map addSpec(String value, Element element, Map map) {
        int pos = value.indexOf('[');
        if (pos >= 0) {
            // list
            String[] specs = value.substring(1, value.length()-1).split(",\\s");
            String tag = null;
            String ind = null;
            StringBuilder subf = new StringBuilder();
            for (String spec : specs) {
                tag = spec.length() > 3 ? spec.substring(0,3) : spec;
                ind = spec.length() > 4 ? spec.substring(3,5) :
                        spec.length() > 3 ? spec.substring(3,4) : null;
                String s = spec.length() > 5 ? spec.substring(5) : null;
                subf.append(s);
            }
            addSpec(element, map, tag, ind, subf.toString() );
        } else {
            // singleton
            // six positions: MAB tag (3 characters), MAB ind1, MAB ind2 (synthetic), MAB subfield (synthetic)
            String tag = value.length() > 3 ? value.substring(0, 3) : value;
            String ind = value.length() > 4 ? value.substring(3,5) :
                    value.length() > 3 ? value.substring(3,4) : null;
            String subf = value.length() > 5 ? value.substring(5) : null;
            addSpec(element, map, tag, ind, subf);
        }
        return map;
    }

    private Map addSpec(Element element, Map map, String tag, String ind, String subf) {
        if (subf != null) {
            // sort subfields
            char[] ch = subf.toCharArray();
            Arrays.sort(ch);
            subf = new String(ch);
        }
        if (ind == null && subf == null) {
            if (!map.containsKey(tag)) {
                map.put(tag, element);
            }
        } else {
            Object o = map.get(tag);
            if (o instanceof Map) {
                addSpec(element, (Map)o, ind, subf, null);
            } else {
                map.put(tag, addSpec(element, new LinkedHashMap(), ind, subf, null));
            }
        }
        return map;
    }*/

}
