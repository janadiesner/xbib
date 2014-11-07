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
package org.xbib.elements.marc.dialects.pica;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.UnmappedKeyListener;
import org.xbib.iri.IRI;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.dialects.pica.DNBPICAXmlReader;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Context;
import org.xbib.rdf.ContextWriter;
import org.xbib.rdf.io.turtle.TurtleWriter;

public class DNBPICAElementsTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(DNBPICAElementsTest.class.getName());

    @Test
    public void testPicaSetup() throws Exception {
        PicaElementBuilderFactory factory = new PicaElementBuilderFactory();
        PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bibdat").start(factory);
        mapper.close();
    }

    @Test
    public void testZdbBib() throws Exception {

        final OurContextOutput output = new OurContextOutput();
        final PicaElementBuilderFactory factory = new PicaElementBuilderFactory() {
            public PicaElementBuilder newBuilder() {
                PicaElementBuilder builder = new PicaElementBuilder();
                builder.addWriter(output);
                return builder;
            }
        };
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        final PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bibdat")
                .setListener(new UnmappedKeyListener<FieldList>() {
                    @Override
                    public void unknown(FieldList key) {
                        unmapped.add(key.toString());
                        logger.warn("unknown field {}", key);
                    }
                })
                .start(factory);
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(new OurTransformer())
                .addListener(mapper)
                .addListener(new OurAdapter());
        final InputStream in = getClass().getResourceAsStream("zdb-oai-bib.xml");
        new DNBPICAXmlReader().setListener(kv).parse(in);
        in.close();
        mapper.close();

        logger.info("counter={}, detected unknown elements = {}",
                output.getCounter(),
                unmapped);
        assertEquals(output.getCounter(), 50);
    }

    class OurTransformer implements StringTransformer {
        @Override
        public String transform(String value) {
            return Normalizer.normalize(value, Normalizer.Form.NFKC);
        }
    }

    class OurAdapter extends KeyValueStreamAdapter<FieldList, String> {
        @Override
        public KeyValueStreamAdapter<FieldList, String> begin() {
            logger.debug("begin object");
            return this;
        }

        @Override
        public KeyValueStreamAdapter<FieldList, String> keyValue(FieldList key, String value) {
            if (logger.isDebugEnabled()) {
                logger.debug("begin");
                for (Field f : key) {
                    logger.debug("tag={} ind={} subf={} data={}",
                            f.tag(), f.indicator(), f.subfieldId(), f.data());
                }
                logger.debug("end");
            }
            return this;
        }

        @Override
        public KeyValueStreamAdapter<FieldList, String> end() {
            logger.debug("end object");
            return this;
        }
    }

    class OurContextOutput implements ContextWriter {

        @Override
        public void write(Context context) throws IOException {
            Resource r = context.getResource();
            IRI id = IRI.builder()
                    .scheme("http")
                    .host("xbib.org")
                    .query("bibdat")
                    .fragment(Long.toString(counter.get())).build();
            r.id(id);
            StringWriter sw = new StringWriter();
            TurtleWriter tw = new TurtleWriter(sw);
            tw.write(context);
            logger.debug("out={}", sw.toString());
            counter.incrementAndGet();
        }
        public final AtomicInteger counter = new AtomicInteger();

        public int getCounter() {
            return counter.get();
        }
    }
}
