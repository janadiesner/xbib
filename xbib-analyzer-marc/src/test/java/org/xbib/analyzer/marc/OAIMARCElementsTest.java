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
package org.xbib.analyzer.marc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import org.testng.annotations.Test;
import org.xbib.elements.output.ElementOutput;
import org.xbib.io.InputStreamService;
import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.DNBPICAXmlReader;
import org.xbib.rdf.ResourceContext;
import org.xml.sax.InputSource;

public class OAIMARCElementsTest {

    private static final Logger logger = LoggerFactory.getLogger(OAIMARCElementsTest.class.getName());

    public void testOAIElements() throws Exception {
        InputStream in =  InputStreamService.getInputStream(URI.create("file:src/test/resources/zdb-oai-marc.xml"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        InputSource source = new InputSource(br);
        MARCBuilder builder = new MARCBuilder();
        builder.addOutput(new ElementOutput() {

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(ResourceContext context, Object info) {
                logger.info("resource = {}", context.resource());
            }

            @Override
            public long getCounter() {
                return 0;
            }
        });
        KeyValueStreamListener listener = new MARCElementMapper("marc/bibliographic").addBuilder(builder);        
        MarcXchange2KeyValue keyvalues = new MarcXchange2KeyValue().setListener(listener);        
        DNBPICAXmlReader reader = new DNBPICAXmlReader(source).setListener(keyvalues);        
        reader.parse();
    }
    
}