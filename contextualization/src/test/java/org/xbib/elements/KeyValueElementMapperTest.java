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
package org.xbib.elements;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.marc.MARCSpecification;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;

import java.util.Map;
import java.util.TreeMap;

public class KeyValueElementMapperTest extends Assert {

    @Test
    public void testMARCSubfields() {
        String value = "100$01$abc";
        Element element = new NullElement();
        Map map = new TreeMap(); // for sorted output in assertEquals matching
        AbstractSpecification specification = new MARCSpecification();
        Map m = specification.addKey(value, element, map);
        value = "100$02$abc";
        element = new NullElement();
        m = specification.addKey(value, element, m);
        value = "100$02$def";
        element = new NullElement();
        m = specification.addKey(value, element, m);
        value = "200$02$abc";
        element = new NullElement();
        m = specification.addKey(value, element, m);
        assertEquals("{100={01={abc=<null>}, 02={abc=<null>, def=<null>}}, 200={02={abc=<null>}}}", m.toString());
        Element e = specification.getElement("100$01$abc", m);
        assertEquals("<null>", e.toString());
        e = specification.getElement("100$01$def", m);
        assertNull(e);
    }

    @Test
    public void testMARCField() {
        String value = "100$01$ab";
        Element element = new NullElement();
        Map map = new TreeMap(); // for sorted output in assertEquals matching
        MARCSpecification specification = new MARCSpecification();
        Map m = specification.addKey(value, element, map);
        Field f = new Field().tag("100").indicator("01");
        Field f1 = new Field(f).subfieldId("a").data("Hello");
        Field f2 = new Field(f).subfieldId("b").data("World");
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        c.add(f2);
        Element e = specification.getElement(c.toKey(), m);
        assertNotNull(e);
    }

}
