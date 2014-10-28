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
package org.xbib.elements.marc;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.UnmappedKeyListener;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.DataField;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.context.ResourceContextWriter;
import org.xbib.rdf.io.turtle.TurtleWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class MARCElementsTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(MARCElementsTest.class.getName());

    @Test
    public void testSetupOfElements() throws Exception {
        MARCElementMapper mapper = new MARCElementMapper("marc/bib").start();
        File file = File.createTempFile("marc-bib-elements.", ".json");
        Writer writer = new FileWriter(file);
        mapper.dump("marc/bib", writer);
        writer.close();
        // test mapper in a MarcXchange listener
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setFormat("MARC21");
        reader.setType("Bibliographic");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        assertNotNull(transformer);
        mapper.close();
    }

    @Test
    public void testStbBonnElements() throws Exception {
        InputStream in = getClass().getResourceAsStream("stb-bonn.mrc");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        File file = File.createTempFile("DE-369.", ".xml");
        Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        final AtomicInteger counter = new AtomicInteger();
        final ResourceContextWriter output = new ResourceContextWriter() {

            @Override
            public void write(ResourceContext context) throws IOException {
                IRI iri = IRI.builder().scheme("http")
                        .host("dummy")
                        .query("dummy")
                        .fragment(Long.toString(counter.getAndIncrement())).build();
                context.getResource().id(iri);
                StringWriter sw = new StringWriter();
                TurtleWriter tw = new TurtleWriter(sw);
                tw.write(context);
            }

        };
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        MARCElementMapper mapper = new MARCElementMapper("marc/bib")
                .setListener(new UnmappedKeyListener<DataField>() {
                    @Override
                    public void unknown(DataField key) {
                        unmapped.add(key.toSpec());
                    }
                })
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        MARCElementBuilder builder = new MARCElementBuilder();
                        builder.addWriter(output);
                        return builder;
                    }
                });
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setFormat("MARC21");
        reader.setType("Bibliographic");
        reader.parse(br);

        mapper.close();
        // check if increment works
        logger.info("unmapped elements = {}", unmapped);
        assertEquals(8676, counter.get());
    }

}
