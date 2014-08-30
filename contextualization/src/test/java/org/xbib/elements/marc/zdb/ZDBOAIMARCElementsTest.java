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
package org.xbib.elements.marc.zdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.AbstractResourceContextWriter;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.context.ResourceContextWriter;
import org.xbib.rdf.io.turtle.TurtleWriter;

public class ZDBOAIMARCElementsTest {

    private static final Logger logger = LoggerFactory.getLogger(ZDBOAIMARCElementsTest.class.getName());

    private final AtomicInteger counter = new AtomicInteger();

    public void testOAIElements() throws Exception {
        final ResourceContextWriter output = new AbstractResourceContextWriter() {

            @Override
            public void write(ResourceContext context) throws IOException {
                Resource r = context.getResource();
                r.id(IRI.builder().host("myindex").query("mytype").fragment(counter.toString()).build());
                StringWriter sw = new StringWriter();
                TurtleWriter tw = new TurtleWriter(sw);
                tw.write(context);
                logger.info("out={}", sw.toString());
                counter.incrementAndGet();
            }

        };
        MARCElementMapper mapper = new MARCElementMapper("marc/zdb/bib")
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        MARCElementBuilder builder = new MARCElementBuilder();
                        builder.addWriter(output);
                        return builder;
                    }
                });
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(mapper);
        InputStream in = getClass().getResourceAsStream("zdb-oai-marc.xml");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        // we need an XML parser here that can produce k/v ...

        //XmlReader reader = new XmlReader().setHandler(...)

        //Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        //reader.parse(source);
        br.close();
        mapper.close();
        // assertEquals ...
    }

}
