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
package org.xbib.marc.xml;

import org.testng.annotations.Test;
import org.xbib.StreamUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeListener;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static org.testng.Assert.assertEquals;

public class MarcXchangeOAITest {

    private final Logger logger = LoggerFactory.getLogger(MarcXchangeOAITest.class.getName());
    

    public void testMarcXMLFromOAI() throws Exception {
        final StringBuilder sb = new StringBuilder();
        MarcXchangeListener listener = new MarcXchangeListener() {

            @Override
            public void beginCollection() {
            }

            @Override
            public void endCollection() {
            }

            @Override
            public void leader(String label) {
                logger.debug("leader="+label);
                sb.append("leader").append("\n");
                sb.append(label).append("\n");
            }

            @Override
            public void beginRecord(String format, String type) {
                logger.debug("beginRecord format="+format + " type="+type);
                sb.append("beginRecord").append("\n");
                sb.append(format).append("\n");
                sb.append(type).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                logger.debug("beginControlField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                logger.debug("endControlField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                logger.debug("beginDataField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                logger.debug("endDataField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                logger.debug("beginSubField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                logger.debug("endsubField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endRecord() {
                logger.debug("endRecord");
                sb.append("endRecord").append("\n");
            }

        };

        InputStream in = getClass().getResourceAsStream("zdb-oai-marc.xml");
        MarcXchangeReader reader = new MarcXchangeReader();
        reader.addListener("Bibliographic", listener);
        reader.parse(in);
        in.close();

        InputStreamReader r = new InputStreamReader(getClass().getResourceAsStream("zdb-oai-marc.txt"));
        StringWriter w = new StringWriter();
        StreamUtil.copy(r, w);
        assertEquals(sb.toString(), w.toString());

    }
}
