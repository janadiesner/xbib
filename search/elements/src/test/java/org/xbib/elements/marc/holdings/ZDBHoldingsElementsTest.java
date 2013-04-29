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
package org.xbib.elements.marc.holdings;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.marc.MARCBuilder;
import org.xbib.elements.marc.MARCBuilderFactory;
import org.xbib.elements.marc.MARCElement;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.elements.output.ElementOutput;
import org.xbib.iri.IRI;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

public class ZDBHoldingsElementsTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(ZDBHoldingsElementsTest.class.getName());

    @Test
    public void testZDBElements() throws Exception {
        final ElementOutput out = new OurElementOutput();
        final Charset UTF8 = Charset.forName("UTF-8");
        final Charset ISO88591 = Charset.forName("ISO-8859-1");
        final InputStream in =
            getClass().getResourceAsStream("zdblokutf8.mrc");
            //new GZIPInputStream(new FileInputStream(System.getProperty("user.home") + "/Daten/zdb/1302zdblokalgesamt.mrc.gz"));
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, ISO88591))) {
            InputSource source = new InputSource(br);
            MARCBuilderFactory factory = new MARCBuilderFactory() {
                public MARCBuilder newBuilder() {
                    MARCBuilder builder = new OurMARCBuilder().addOutput(out);
                    return builder;
                }
            };
            MARCElementMapper mapper = new MARCElementMapper("marc/holdings")
                    .detectUnknownKeys(true)
                    .start(factory);
            MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                        @Override
                        public String transform(String value) {
                            return Normalizer.normalize(
                                    new String(value.getBytes(ISO88591), UTF8),
                                    Normalizer.Form.NFKC);
                        }
                    })
                    .addListener(mapper)
                    .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                        @Override
                        public void keyValue(FieldCollection key, String value) {
                            logger.debug("begin");
                            for (Field f : key) {
                                logger.debug("tag={} ind={} subf={} data={}",
                                        f.tag(), f.indicator(), f.subfieldId(), f.data());
                            }
                            logger.debug("end");
                        }

                    });
            Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
            reader.setProperty(Iso2709Reader.FORMAT, "MARC");
            reader.setProperty(Iso2709Reader.TYPE, "Holdings");
            reader.parse(source);
            mapper.close();
            logger.info("zdb holdings counter = {}", out.getCounter());
            logger.info("zdb holdings unknown keys = {}", mapper.unknownKeys());
        }
        assertEquals(out.getCounter(), 293);
    }

    class OurMARCBuilder extends MARCBuilder {

        @Override
        public void build(MARCElement element, FieldCollection fields, String value) {
            if (context().resource().id() == null) {
                IRI id = IRI.builder().scheme("http").host("xbib.org").fragment(Long.toString(context().increment())).build();
                context().resource().id(id);
            }
        }
    }

    class OurElementOutput implements ElementOutput {
        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public void enabled(boolean enabled) {
        }

        @Override
        public void output(ResourceContext context) throws IOException {
            if (!context.resource().isEmpty()) {
                Resource r = context.resource();
                r.id(IRI.builder().host("myindex").query("mytype").fragment(r.id().getFragment()).build());
                StringBuilder sb = new StringBuilder();
                Iterator<Triple> it = r.iterator();
                while (it.hasNext()) {
                    sb.append(it.next().toString()).append("\n");
                }
                logger.debug("out={}", sb.toString());
                counter.incrementAndGet();
            }
        }

        @Override
        public long getCounter() {
            return counter.get();
        }
    }

    final AtomicLong counter = new AtomicLong();

}