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
import org.xbib.rdf.IdentifiableProperty;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.simple.SimpleProperty;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.rdf.simple.SimpleTriple;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class EuropeanaEDMReaderTest {

    private final Logger logger = LoggerFactory.getLogger(EuropeanaEDMReaderTest.class.getName());

    @Test
    public void testEuropeana() throws Exception {
        String filename = "/org/xbib/rdf/io/rdfxml/oai_edm.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        SimpleResourceContext resourceContext = new SimpleResourceContext();

        RdfXmlReader reader = new RdfXmlReader();
        reader.setTripleListener(new GeoJSONFilter(resourceContext));
        reader.parse(new InputSource(in));

        String index = "index";
        String type = "type";

        logger.info("{} resources", resourceContext.getResources().size());
        for (Resource resource : resourceContext.getResources()) {
            //logger.info("before id=" + resource.id());
            IRI iri = IRI.builder().scheme("http")
                    .host(index)
                    .query(type)
                    .fragment(resource.id().toString()).build();
            resource.id(iri);

            //logger.info("after id=" + resource.id());
            StringWriter sw = new StringWriter();
            NTripleWriter writer = new NTripleWriter().output(sw);
            writer.write(resource);
            logger.info("output=" + sw.toString());
        }

    }

    class GeoJSONFilter implements TripleListener {

        ResourceContext<Resource> resourceContext;

        String lat;

        String lon;

        GeoJSONFilter(ResourceContext<Resource> resourceContext) {
            this.resourceContext = resourceContext;
        }

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
            return this;
        }

        @Override
        public TripleListener triple(Triple triple) {
            resourceContext.triple(triple);
            if (triple.predicate().id().toString().equals(GEO_LAT.toString())) {
                lat = triple.object().toString();
            } else if (triple.predicate().id().toString().equals(GEO_LON.toString())) {
                lon = triple.object().toString();
            }
            if (lat != null  && lon != null) {
                // create GeoJSON
                resourceContext.triple(new SimpleTriple(triple.subject(), "location", lon));
                resourceContext.triple(new SimpleTriple(triple.subject(), "location", lat));
                lon = null;
                lat = null;
            }
            return this;
        }

        @Override
        public TripleListener end() {
            return this;
        }
    }

    private final static IRI GEO_LAT = IRI.create("http://www.w3.org/2003/01/geo/wgs84_pos#lat");

    private final static IRI GEO_LON = IRI.create("http://www.w3.org/2003/01/geo/wgs84_pos#long");


}
