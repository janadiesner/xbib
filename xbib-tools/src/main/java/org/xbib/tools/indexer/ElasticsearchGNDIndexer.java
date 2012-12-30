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
package org.xbib.tools.indexer;

import org.xbib.elasticsearch.ElasticsearchIndexer;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.io.InputStreamService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.io.turtle.TurtleReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Elasticsearch GND indexer
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ElasticsearchGNDIndexer {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchGNDIndexer.class.getName());

    public static void main(String[] args) {

        try {
            OptionParser parser = new OptionParser() {

                {
                    accepts("gndfile").withRequiredArg().ofType(String.class).required();
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("ElasticsearchGNDIndexer");
                System.err.println("--gndfile <uri>");
                System.err.println("--elasticsearch <uri>");
                System.err.println("--index <name>");
                System.err.println("--type <name>");
                System.exit(1);
            }
            final String uriStr = (String) options.valueOf("gndfile");
            final String elasticsearch = (String) options.valueOf("elasticsearch");
            final String index = (String) options.valueOf("index");
            final String type = (String) options.valueOf("type");
            URI uri = URI.create(uriStr);
            InputStream in = InputStreamService.getInputStream(uri);
            if (in == null) {
                throw new IOException("file not found: " + uriStr);
            }
            final ElasticBuilder builder = new ElasticBuilder(elasticsearch, index, type);
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    try {
                        System.err.println("received interruption, closing, please wait...");
                        builder.close();
                        System.err.println("closed");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            TurtleReader reader = new TurtleReader(IRI.create("http://d-nb.info/gnd/"));
            reader.setListener(builder);
            reader.parse(in);
            builder.close();
            System.err.println("done, indexed resources have " + builder.getTripleCounter() + " triples.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static class ElasticBuilder implements StatementListener {

        private final ElasticsearchResourceSink sink;
        private final ResourceContext context = new SimpleResourceContext();
        private long triplecounter;
        private Resource resource;

        ElasticBuilder(String esURI, String index, String type) throws IOException {
            ElasticsearchIndexer elasticsearch = new ElasticsearchIndexer();
            elasticsearch.newClient(URI.create(esURI),false).setIndex(index).setType(type);
            sink = new ElasticsearchResourceSink(elasticsearch);
            resource = context.newResource();
        }

        public void close() throws IOException {
            flush();
            sink.flush();
        }

        @Override
        public void newIdentifier(IRI uri) {
            flush();
            resource.id(uri);
        }

        @Override
        public void statement(Statement statement) {
            resource.add(statement);
            triplecounter++;
        }

        public long getTripleCounter() {
            return triplecounter;
        }

        private void flush() {
            try {
                sink.output(context);
            } catch (IOException e) {
                logger.error("flush failed: {}", e.getMessage(), e);
            }
            context.reset();
            resource = context.newResource();
        }

    }
}
