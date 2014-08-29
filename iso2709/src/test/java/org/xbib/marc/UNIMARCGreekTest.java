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
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.normalize.ValueNormalizer;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xbib.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class UNIMARCGreekTest {

    private final Logger logger = LoggerFactory.getLogger(UNIMARCGreekTest.class.getSimpleName());

    private final Charset ISO88591 = Charset.forName("ISO-8859-1"); // 8 bit

    private final Charset UTF8 = Charset.forName("UTF-8");

    @Test
    public void testUNIMARC() throws IOException, SAXException {
        for (String s : new String[]{
                "serres.mrc"
        }) {
            InputStream in = getClass().getResourceAsStream(s);
            try (InputStreamReader r = new InputStreamReader(in, ISO88591)) {

                final Iso2709Reader reader = new Iso2709Reader()
                        .setValueNormalizer(new ValueNormalizer() {
                            @Override
                            public String normalize(String value) {
                                return XMLUtil.clean(new String(value.getBytes(ISO88591), UTF8));
                            }
                        });
                new File("target").mkdirs();
                FileWriter w = new FileWriter("target/" + s + ".xml");
                MarcXchangeWriter writer = new MarcXchangeWriter(w);
                reader.setFormat("UNIMARC").setType("Bibliographic");
                reader.setMarcXchangeListener(writer);
                writer.startDocument();
                writer.beginCollection();
                reader.parse(new InputSource(r));
                writer.endCollection();
                writer.endDocument();
                logger.error("err?", writer.getException());
                w.close();
            }
        }
    }

}
