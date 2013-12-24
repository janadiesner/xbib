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
package org.xbib.elasticsearch.tools.feed;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;

import org.xbib.elasticsearch.ResourceSink;
import org.xbib.elasticsearch.support.client.IngestClient;
import org.xbib.elasticsearch.support.client.MockIngestClient;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.AbstractPipeline;
import org.xbib.pipeline.SimplePipelineExecutor;
import org.xbib.pipeline.Pipeline;
import org.xbib.io.InputService;
import org.xbib.io.file.Finder;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.element.CounterElement;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.turtle.TurtleReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.options.OptionParser;
import org.xbib.options.OptionSet;
import org.xbib.util.FormatUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Elasticsearch Freebase indexer
 *
 */
public class Freebase extends AbstractPipeline<CounterElement> {

    private static final Logger logger = LoggerFactory.getLogger(Freebase.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static CounterElement fileCounter = new CounterElement().set(new AtomicLong(0L));

    private final static AtomicLong docCounter = new AtomicLong(0L);

    private final static AtomicLong charCounter = new AtomicLong(0L);

    private static Queue<URI> input;

    private static OptionSet options;

    private static ResourceSink sink;

    private static IRI base;

    private boolean done = false;

    public static void main(String[] args) {

        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(4 * Runtime.getRuntime().availableProcessors());
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(4 * Runtime.getRuntime().availableProcessors());
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.rdf.gz");
                    accepts("base").withRequiredArg().ofType(String.class).required().defaultsTo("http://freebase.com/");
                    accepts("help");
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + EZB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.rdf.gz)" + lf
                        + " --threads <n>          the number of threads (optional, default: <num-of=cpus)"
                        + " --base <IRI>           a base IRI for Turtle to resolve against (required, default: http://freebase.com/)" + lf
                );
                System.exit(1);
            }
            input = new Finder((String)options.valueOf("pattern"))
                    .find((String)options.valueOf("path"))
                    .pathSorted()
                    .getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {},  threads = {}", input, threads);

            final String elasticsearch = (String) options.valueOf("elasticsearch");
            final String index = (String) options.valueOf("index");
            final String type = (String) options.valueOf("type");
            final URI esURI = URI.create(elasticsearch);
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            boolean mock = (Boolean)options.valueOf("mock");

            final IngestClient es = mock ? new MockIngestClient() : new IngestClient();

            es.maxActionsPerBulkRequest(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .setIndex(index)
                    .setType(type)
                    .newClient(esURI)
                    .waitForCluster(ClusterHealthStatus.GREEN, TimeValue.timeValueSeconds(30));

            sink = new ResourceSink(es);

            base = IRI.create((String)options.valueOf("base"));

            long t0 = System.currentTimeMillis();
            SimplePipelineExecutor service = new SimplePipelineExecutor()
                    .concurrency(threads)
                    .provider(new PipelineProvider() {
                        @Override
                        public Pipeline get() {
                            return new Freebase();
                        }
                    })
                    .execute();
            long t1 = System.currentTimeMillis();
            long docs = docCounter.get();
            long bytes = charCounter.get();
            double dps = docs * 1000.0 / (double)(t1 - t0);
            double avg = bytes / (docs + 1); // avoid div by zero
            double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
            String t = FormatUtil.formatMillis(t1 - t0);
            String byteSize = FormatUtil.convertFileSize(bytes);
            String avgSize = FormatUtil.convertFileSize(avg);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            logger.info("Converting complete. {} files, {} docs, {} = {} ms, {} = {} chars, {} = {} avg size, {} dps, {} MB/s",
                    fileCounter,
                    docs,
                    t,
                    (t1-t0),
                    bytes,
                    byteSize,
                    avgSize,
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));

            service.shutdown();

            es.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private Freebase() {
    }

    @Override
    public void close() throws IOException {
        input.clear();
    }

    @Override
    public boolean hasNext() {
        return !done && !input.isEmpty();
    }

    @Override
    public CounterElement next() {
        URI uri = input.poll();
        done = uri == null;
        if (done) {
            return fileCounter;
        }
        try {
            InputStream in = InputService.getInputStream(uri);
            ElasticBuilder builder = new ElasticBuilder(sink);
            new TurtleReader(base)
                    .setTripleListener(builder)
                    .parse(in);
            in.close();
            fileCounter.get().incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while parsing from stream: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

    private class ElasticBuilder implements TripleListener {

        private final ResourceSink sink;

        private final ResourceContext context = new SimpleResourceContext();

        private Resource resource;

        ElasticBuilder(ResourceSink sink) throws IOException {
            this.sink = sink;
            resource = context.newResource();
        }

        public void close() throws IOException {
            flush();
            sink.flush();
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

        private void flush() {
            try {
                sink.output(context, context.contentBuilder());
            } catch (IOException e) {
                logger.error("flush failed: {}", e.getMessage(), e);
            }
        }

    }
}
