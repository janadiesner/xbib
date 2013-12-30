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
package org.xbib.analyzer.dublincore;

import java.io.IOException;
import java.io.StringReader;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.CountableElementOutput;
import org.xbib.io.keyvalue.KeyValueReader;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.xcontent.ContentBuilder;

public class DublinCoreBuilderTest extends Assert {

    private final Logger logger = LoggerFactory.getLogger(DublinCoreBuilderTest.class.getName());

    @Test
    public void testDublinCoreBuilder() throws Exception {
        StringReader sr = new StringReader("100=John Doe\n200=Hello Word\n300=2012\n400=1");
        CountableElementOutput<DublinCoreContext,Resource> output = new CountableElementOutput<DublinCoreContext, Resource>() {

            @Override
            public void output(DublinCoreContext context, ContentBuilder builder) throws IOException {
                logger.info("resource = {}", context.resource());
                counter.incrementAndGet();
            }

        };
        
        DublinCoreBuilder builder = new DublinCoreBuilder().addOutput(output);
        DublinCoreElementMapper mapper = new DublinCoreElementMapper("dublincore").start(builder);
        try (KeyValueReader reader = new KeyValueReader(sr).addListener(mapper)) {
            while (reader.readLine() != null);
        }
        mapper.close();
        assertEquals(output.getCounter() > 0L, true);
    }
}
