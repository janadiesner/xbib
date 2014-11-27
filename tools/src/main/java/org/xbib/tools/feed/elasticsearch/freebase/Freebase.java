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
package org.xbib.tools.feed.elasticsearch.freebase;

import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.io.turtle.TurtleContentParser;
import org.xbib.tools.Feeder;

import java.io.InputStream;
import java.net.URI;

/**
 * Elasticsearch Freebase indexer
 */
public class Freebase extends Feeder {

    private static final Logger logger = LoggerFactory.getLogger(Freebase.class.getName());

    @Override
    public String getName() {
        return "freebase-turtle-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new Freebase();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        //ElasticBuilder builder = new ElasticBuilder(elasticsearchRdfXContentGenerator);
        IRI base = IRI.create(settings.get("base"));
        new TurtleContentParser(in).setBaseIRI(base)
                .parse();
        in.close();
    }
/*
    private class ElasticBuilder implements Triple.Builder {

        private final ElasticsearchRdfXContentGenerator elasticsearchRdfXContentGenerator;

        private final Context context = new MemoryContext();

        private Resource resource;

        ElasticBuilder(ElasticsearchRdfXContentGenerator elasticsearchRdfXContentGenerator) throws IOException {
            this.elasticsearchRdfXContentGenerator = elasticsearchRdfXContentGenerator;
            resource = context.newResource();
        }

        public void close() throws IOException {
            flush();
        }

        @Override
        public Triple.Builder begin() {
            return this;
        }

        @Override
        public Triple.Builder startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public Triple.Builder endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public ElasticBuilder newIdentifier(IRI uri) {
            flush();
            resource.id(uri);
            return this;
        }

        @Override
        public ElasticBuilder triple(Triple triple) {
            resource.add(triple);
            return this;
        }

        @Override
        public Triple.Builder end() {
            return this;
        }

        private void flush() {
            try {
                elasticsearchRdfXContentGenerator.write(context);
            } catch (IOException e) {
                logger.error("flush failed: {}", e.getMessage(), e);
            }
        }

    }
*/
}
