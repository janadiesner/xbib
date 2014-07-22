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
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.Normalizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.dialects.pica.DNBPICAXmlReader;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.AbstractResourceContextWriter;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xml.sax.InputSource;

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

        final OurContextResourceOutput output = new OurContextResourceOutput();
        final PicaElementBuilderFactory factory = new PicaElementBuilderFactory() {
            public PicaElementBuilder newBuilder() {
                PicaElementBuilder builder = new PicaElementBuilder();
                builder.addWriter(output);
                return builder;
            }
        };
        final PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bibdat")
                .detectUnknownKeys(true)
                .start(factory);
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .transformer(new OurTransformer())
                .addListener(mapper)
                .addListener(new OurAdapter());
        final InputStream in = getClass().getResourceAsStream("zdb-oai-bib.xml");
        final InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
        new DNBPICAXmlReader().setListener(kv).parse(source);
        in.close();
        mapper.close();

        logger.info("counter={}, detected unknown elements = {}",
                output.getCounter(),
                mapper.unknownKeys());
        assertEquals(output.getCounter(), 50);
    }

    class OurTransformer implements MarcXchange2KeyValue.FieldDataTransformer {
        @Override
        public String transform(String value) {
            return Normalizer.normalize(value, Normalizer.Form.NFKC);
        }
    }

    class OurAdapter extends KeyValueStreamAdapter<FieldCollection, String> {
        @Override
        public KeyValueStreamAdapter<FieldCollection, String> begin() {
            logger.debug("begin object");
            return this;
        }

        @Override
        public KeyValueStreamAdapter<FieldCollection, String> keyValue(FieldCollection key, String value) {
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
        public KeyValueStreamAdapter<FieldCollection, String> end() {
            logger.debug("end object");
            return this;
        }
    }

    class OurContextResourceOutput extends AbstractResourceContextWriter {

        @Override
        public void write(ResourceContext context) throws IOException {
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
