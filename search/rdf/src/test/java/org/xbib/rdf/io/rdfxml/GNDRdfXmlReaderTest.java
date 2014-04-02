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
package org.xbib.rdf.io.rdfxml;

import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.rdf.xcontent.DefaultContentBuilder;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class GNDRdfXmlReaderTest {

    private final Logger logger = LoggerFactory.getLogger(GNDRdfXmlReaderTest.class.getName());

    @Test
    public void testGNDfromRdfXmltoTurtle() throws Exception {
        String filename = "/org/xbib/rdf/io/rdfxml/GND.rdf";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        StringWriter sw = new StringWriter();
        TurtleWriter writer  = new TurtleWriter()
                .output(sw);
        RdfXmlReader reader = new RdfXmlReader();
        reader.setTripleListener(writer);
        reader.parse(new InputSource(in));
        writer.close();
        logger.info("gnd = {}", sw.toString());
    }

    @Test
    public void testGNDContentBuilder() throws Exception {
        String filename = "/org/xbib/rdf/io/rdfxml/GND.rdf";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        TripleContentBuilder tripleContentBuilder = new TripleContentBuilder();
        RdfXmlReader reader = new RdfXmlReader();
        reader.setTripleListener(tripleContentBuilder);
        reader.parse(new InputSource(in));
    }

    class TripleContentBuilder implements TripleListener {

        Resource resource;

        @Override
        public TripleListener begin() {
            return this;
        }

        @Override
        public TripleListener startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public TripleListener endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public TripleListener newIdentifier(IRI identifier) {
            try {
                if (resource != null) {
                    output(resource);
                }
            } catch (IOException e) {
                //
            }
            resource = new SimpleResource();
            resource.id(identifier);
            return this;
        }

        @Override
        public TripleListener triple(Triple triple) {
            logger.info("{} {} {} -> {} {} {}",
                    triple.subject().getClass(),
                    triple.predicate().getClass(),
                    triple.object().getClass(),
                    triple.subject(),
                    triple.predicate(),
                    triple.object()
                    );
            resource.add(triple);
            return this;
        }

        @Override
        public TripleListener end() {
            try {
                if (resource != null) {
                    output(resource);
                }
            } catch (IOException e) {
                //
            }
            return this;
        }

        private void output(Resource resource) throws IOException {
            ResourceContext context = new SimpleResourceContext();

            DefaultContentBuilder contentBuilder = new DefaultContentBuilder();
            String s = contentBuilder.build(context, resource);
            logger.info("{}", s);
        }
    }

}
