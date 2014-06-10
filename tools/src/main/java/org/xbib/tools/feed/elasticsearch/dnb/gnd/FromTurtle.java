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
package org.xbib.tools.feed.elasticsearch.dnb.gnd;

import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineException;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.turtle.TurtleReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.Feeder;
import org.xbib.xml.namespace.XmlNamespaceContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * GND ingest
 */
public class FromTurtle extends Feeder implements TripleListener {

    private static final Logger logger = LoggerFactory.getLogger(FromTurtle.class.getSimpleName());

    private static final ResourceContext context = new SimpleResourceContext();

    private Resource resource;

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromTurtle();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        resource = context.newResource();
        IRI id = IRI.builder().scheme("http").host("d-nb.info").path("/gnd/").build();
        XmlNamespaceContext xmlContext = XmlNamespaceContext.getDefaultInstance();

        xmlContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        xmlContext.addNamespace("geo", "http://rdvocab.info/");
        xmlContext.addNamespace("rda", "http://purl.org/dc/elements/1.1/");
        xmlContext.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        xmlContext.addNamespace("sf", "http://www.opengis.net/ont/sf#");
        xmlContext.addNamespace("isbd", "http://iflastandards.info/ns/isbd/elements/");
        xmlContext.addNamespace("gndo", "http://d-nb.info/standards/elementset/gnd#");
        xmlContext.addNamespace("dcterms", "http://purl.org/dc/terms/");
        xmlContext.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        xmlContext.addNamespace("marcRole", "http://id.loc.gov/vocabulary/relators/");
        xmlContext.addNamespace("lib", "http://purl.org/library/");
        xmlContext.addNamespace("umbel", "http://umbel.org/umbel#");
        xmlContext.addNamespace("bibo", "http://purl.org/ontology/bibo/");
        xmlContext.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
        xmlContext.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        xmlContext.addNamespace("skos", "http://www.w3.org/2004/02/skos/core#");

        TurtleReader reader = new TurtleReader(id)
                .context(xmlContext);
        reader.setTripleListener(this);
        reader.parse(in);
    }

    @Override
    public TripleListener begin() {
        return this;
    }

    @Override
    public TripleListener startPrefixMapping(String prefix, String uri) {
        // TODO add namespace?
        return this;
    }

    @Override
    public TripleListener endPrefixMapping(String prefix) {
        return this;
    }

    @Override
    public FromTurtle newIdentifier(IRI uri) {
        try {
            sink.output(context, context.getResource(), context.getContentBuilder());
        } catch (IOException e) {
            logger.error("flush failed: {}", e.getMessage(), e);
        }
        resource = context.newResource();
        resource.id(uri);
        return this;
    }

    @Override
    public FromTurtle triple(Triple triple) {
        resource.add(triple);
        return this;
    }

    @Override
    public TripleListener end() {
        return this;
    }

    @Override
    public void newRequest(Pipeline pipeline, PipelineRequest request) {

    }

    @Override
    public void error(Pipeline pipeline, PipelineRequest request, PipelineException error) {
        logger.error(error.getMessage(), error);

    }
}
