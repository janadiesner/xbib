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

import org.testng.annotations.Test;
import org.xbib.io.field.FieldSortable;

import static org.testng.Assert.assertEquals;

public class FieldTest {

    @Test
    public void testFieldData() {
        Field f = new Field().tag("100").indicator("").data("Hello World");
        assertEquals(f.data(), "Hello World");
    }

    @Test
    public void testFieldDataSortable() {
        Field f = new Field().tag("100").indicator("").data("\u0098Hello \u009CWorld");
        assertEquals(f.data(), "\u0098Hello \u009CWorld");
        assertEquals(f.dataSortable(), "World");
    }

    @Test
    public void sortableTest() {
        Field f = new Field().tag("331").indicator("")
                .subfieldId(null)
                .data('\u0098' + "Der" + '\u009c' + " kleine Prinz");

        assertEquals(f.data(), FieldSortable.NON_SORTABLE_BEGIN + "Der" + FieldSortable.NON_SORTABLE_END + " kleine Prinz");
        assertEquals(f.dataSortable(), " kleine Prinz");
    }

    @Test
    public void testFieldCollection() {
        Field f = new Field().tag("016").indicator("").subfieldId(null).data(null);
        Field f1 = new Field(f).subfieldId("1");
        Field f2 = new Field(f).subfieldId("2");
        Field f3 = new Field(f).subfieldId("3");
        FieldCollection c = new FieldCollection();
        c.add(f);
        c.add(f1);
        c.add(f2);
        c.add(f3);
        assertEquals(c.toSpec(), "016$123");
    }
}
