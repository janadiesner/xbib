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
package org.xbib.elements.marc.extensions.pica;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.output.ElementOutput;
import org.xbib.iri.IRI;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.DNBPICAXmlReader;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xml.sax.InputSource;

public class DNBPICAElementsTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(DNBPICAElementsTest.class.getName());

    @Test
    public void testPicaSetup() throws Exception {
        PicaBuilderFactory factory = new PicaBuilderFactory();
        PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bib").start(factory);
        mapper.close();
    }

    @Test
    public void testZdbBib() throws Exception {

        final AtomicLong counter = new AtomicLong();
        final ElementOutput output = new ElementOutput<ResourceContext>() {

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void enabled(boolean enabled) {
            }

            @Override
            public void output(ResourceContext context) throws IOException {
                if (context != null) {
                    Resource r = context.resource();
                    // index/type adressing via resource ID
                    r.id(new IRI().host("myindex").query("mytype").fragment(r.id().getFragment()).build());
                    // dump all RDF triples
                    StringBuilder sb = new StringBuilder();
                    Iterator<Triple> it = r.iterator();
                    while (it.hasNext()) {
                        sb.append(it.next().toString()).append("\n");
                    }
                    logger.info("out={}", sb.toString());
                    counter.incrementAndGet();
                }
            }

            @Override
            public long getCounter() {
                return counter.get();
            }
        };

        PicaBuilderFactory factory = new PicaBuilderFactory() {
            public PicaBuilder newBuilder() {
                return new PicaBuilder().addOutput(output);
            }
        };

        PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bib")
                .detectUnknownKeys(true)
                .start(factory);

        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(
                                value,
                                Normalizer.Form.NFKC);
                    }
                })
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                    @Override
                    public void begin() {
                        logger.debug("begin object");
                    }

                    @Override
                    public void keyValue(FieldCollection key, String value) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("begin");
                            for (Field f : key) {
                                logger.debug("tag={} ind={} subf={} data={}",
                                        f.tag(), f.indicator(), f.subfieldId(), f.data());
                            }
                            logger.debug("end");
                        }
                    }

                    @Override
                    public void end() {
                        logger.debug("end object");
                    }

                    @Override
                    public void end(Object info) {
                        logger.debug("end object (info={})", info);
                    }
                });

        InputStream in = getClass().getResourceAsStream("zdb-oai-bib.xml");
        InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
        new DNBPICAXmlReader(source).setListener(kv).parse();
        in.close();
        mapper.close();

        logger.info("counter={}, detected unknown elements = {}",
                counter.get(),
                mapper.unknownKeys());
        assertEquals(counter.get(), 50);

    }
}