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
import org.xbib.elements.CountableElementOutput;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.ValueNormalizer;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public class UNIMARCElementsTest extends Assert {

    private final Logger logger = LoggerFactory.getLogger(UNIMARCElementsTest.class.getName());

    private final Charset ISO88591 = Charset.forName("ISO-8859-1"); // 8 bit

    private final Charset UTF8 = Charset.forName("UTF-8");

    @Test
    public void testSetupOfElements() throws Exception {
        MARCElementMapper mapper = new MARCElementMapper("unimarc/bib").start();
        Writer writer = new FileWriter("target/unimarc-mapping.json");
        mapper.dump("unimarc/bib", writer);
        writer.close();
        // test the mapper in a MarcXchange listener
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MARC");
        reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        assertNotNull(transformer);
        mapper.close();
    }

    @Test
    public void testUNIMARC() throws IOException, SAXException,
            ParserConfigurationException, TransformerException {
        for (String s : new String[]{
                "/org/xbib/elements/marc/dialects/unimarc/serres.mrc"
        }) {
            InputStream in = getClass().getResourceAsStream(s);
            try (InputStreamReader r = new InputStreamReader(in, ISO88591)) {
                final CountableElementOutput output = new CountableElementOutput<ResourceContext, Resource>() {

                    @Override
                    public void output(ResourceContext context, ContentBuilder<ResourceContext, Resource> builder) throws IOException {
                        IRI iri = IRI.builder().scheme("http")
                                .host("dummyindex")
                                .query("dummytype")
                                .fragment(Long.toString(counter.getAndIncrement())).build();
                        context.resource().id(iri);
                        StringWriter sw = new StringWriter();
                        TurtleWriter tw = new TurtleWriter().output(sw);
                        tw.write(context.resource());
                        //logger.debug("out={}", sw.toString());
                    }

                };
                MARCElementMapper mapper = new MARCElementMapper("marc/bib")
                        .detectUnknownKeys(true)
                        .start(new MARCElementBuilderFactory() {
                            public MARCElementBuilder newBuilder() {
                                return new MARCElementBuilder().addOutput(output);
                            }
                        });
                MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);

                final Iso2709Reader reader = new Iso2709Reader()
                        .setValueNormalizer(new ValueNormalizer() {
                            @Override
                            public String normalize(String value) {
                                return value == null ? null : new String(value.getBytes(ISO88591), UTF8);
                            }
                        })
                        .setMarcXchangeListener(kv);
                reader.setProperty(Iso2709Reader.FORMAT, "MARC");
                reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");

                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                InputSource source = new InputSource(r);
                new File("target/" + s).getParentFile().mkdirs();
                FileOutputStream out = new FileOutputStream("target/" + s + ".xml");
                Writer w = new OutputStreamWriter(out, UTF8);
                StreamResult target = new StreamResult(w);
                transformer.transform(new SAXSource(reader, source), target);
                mapper.close();
                // check if increment works
                logger.info("unknown elements = {}", mapper.unknownKeys());
                assertEquals(51279, output.getCounter());
            }


        }
    }

}
