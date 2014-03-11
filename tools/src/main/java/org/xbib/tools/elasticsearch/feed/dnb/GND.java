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
package org.xbib.tools.elasticsearch.feed.dnb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.tools.elasticsearch.Feeder;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.turtle.TurtleReader;
import org.xbib.rdf.simple.SimpleResourceContext;

/**
 * GND ingest
 */
public class GND extends Feeder implements TripleListener {

    private static final Logger logger = LoggerFactory.getLogger(GND.class.getSimpleName());

    private static final ResourceContext context = new SimpleResourceContext();

    private Resource resource;

    public static void main(String[] args) {
        try {
            new GND()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private GND() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new GND();
            }
        };
    }

    @Override
    public GND prepare(Ingest output) {
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        resource = context.newResource();
        IRI id = IRI.builder().scheme("http").host("d-nb.info").path("/gnd/").build();
        TurtleReader reader = new TurtleReader(id);
        reader.setTripleListener(this);
        reader.parse(in);
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
    public GND newIdentifier(IRI uri) {
        try {
            sink.output(context, context.contentBuilder());
        } catch (IOException e) {
            logger.error("flush failed: {}", e.getMessage(), e);
        }
        context.reset();
        resource = context.newResource();
        resource.id(uri);
        return this;
    }

    @Override
    public GND triple(Triple triple) {
        resource.add(triple);
        return this;
    }

}
