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
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class MABTest {

    /**
     * Broken right now. Needs fix for subfield-less mode.
     *
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void testZDBMAB() throws IOException, SAXException {
        InputStream in = getClass().getResourceAsStream("1217zdbtit.dat");
        FileOutputStream out = new FileOutputStream("target/zdb.xml");
        try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
            read(new InputStreamReader(in, "x-MAB"), w);
        }
    }

    @Test
    public void testCreateMABXML() throws IOException, SAXException {
        String s = "test.groupstream";
        InputStream in = getClass().getResourceAsStream(s);
        FileOutputStream out = new FileOutputStream("target/" + s + ".xml");
        try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
            read(new InputStreamReader(in, "x-MAB"), w);
        }
    }

    private void read(Reader source, Writer target) throws IOException, SAXException {
        Iso2709Reader reader = new Iso2709Reader();
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        MarcXchangeWriter writer = new MarcXchangeWriter(target);
        reader.setMarcXchangeListener(writer);
        writer.startDocument();
        writer.beginCollection();
        reader.parse(new InputSource(source));
        writer.endCollection();
        writer.endDocument();
    }
}
