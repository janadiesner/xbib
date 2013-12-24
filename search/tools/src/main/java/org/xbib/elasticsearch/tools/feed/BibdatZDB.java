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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.ResourceSink;
import org.xbib.elasticsearch.support.client.IngestClient;
import org.xbib.elasticsearch.support.client.MockIngestClient;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilder;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilderFactory;
import org.xbib.elements.marc.dialects.pica.PicaElementMapper;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.AbstractPipeline;
import org.xbib.pipeline.SimplePipelineExecutor;
import org.xbib.pipeline.Pipeline;
import org.xbib.io.InputService;
import org.xbib.io.file.Finder;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.DNBPICAXmlReader;
import org.xbib.pipeline.element.CounterElement;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.options.OptionParser;
import org.xbib.options.OptionSet;
import org.xbib.util.FormatUtil;
import org.xml.sax.InputSource;

/**
 *
 * Index bib addresses into Elasticsearch
 *
 */
public final class BibdatZDB extends AbstractPipeline<CounterElement> {

    private final static Logger logger = LoggerFactory.getLogger(BibdatZDB.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static CounterElement fileCounter = new CounterElement().set(new AtomicLong(0L));

    private static Queue<URI> input;

    private boolean done = false;

    private static int pipelines;

    private static String index;

    private static String type;

    private static ResourceSink<ResourceContext, Resource> sink;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("overwrite").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("pipelines").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.xml");
                }
            };
            OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + BibdatZDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                );
                System.exit(1);
            }

            input = new Finder((String) options.valueOf("pattern")).
                    find((String) options.valueOf("path"))
                    .getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {}, threads = {}", input, threads);

            URI esURI = URI.create(options.valueOf("elasticsearch").toString());
            index = (String) options.valueOf("index");
            type = (String) options.valueOf("type");
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            pipelines = (Integer) options.valueOf("pipelines");
            boolean mock = (Boolean) options.valueOf("mock");

            final IngestClient es = mock ? new MockIngestClient() : new IngestClient();

            es.maxActionsPerBulkRequest(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .waitForCluster();

            logger.info("creating new index ...");
            es.setIndex(index)
                    .setType(type)
                    .dateDetection(false)
                    .newIndex();
            logger.info("... new index created");

            sink = new ResourceSink(es);

            long t0 = System.currentTimeMillis();
            SimplePipelineExecutor service = new SimplePipelineExecutor()
                    .concurrency(threads)
                    .provider(new PipelineProvider() {
                        @Override
                        public Pipeline get() {
                            return new BibdatZDB();
                        }
                    })
                    .execute()
                    .waitFor();

            long t1 = System.currentTimeMillis();
            long docs = out.getCounter();
            long bytes = es.getTotalSizeInBytes();
            double dps = docs * 1000.0 / (double)(t1 - t0);
            double avg = bytes / (docs + 1.0); // avoid div by zero
            double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
            String t = TimeValue.timeValueMillis(t1 - t0).format();
            String byteSize = FormatUtil.convertFileSize(bytes);
            String avgSize = FormatUtil.convertFileSize(avg);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            logger.info("Indexing complete. {} files, {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
                    fileCounter, docs, t, (t1-t0), byteSize, bytes,
                    avgSize,
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));

            service.shutdown();
            es.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
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
            PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bib")
                    .pipelines(pipelines)
                    .detectUnknownKeys(true)
                    .start(factory);

            MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                        @Override
                        public String transform(String value) {
                            // DNB Pica contains denormalized UTF-8, use
                            // compatibility composing (best for search engines)
                            return Normalizer.normalize(
                                    value,
                                    Normalizer.Form.NFKC);
                        }
                    })
                    .addListener(mapper);
                    /*.addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                        @Override
                        public void begin() {
                            logger.debug("begin object");
                        }

                        @Override
                        public void keyValue(FieldCollection key, String value) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("begin");
                                for (Field f : key) {
                                    logger.debug("tag={} ind={} subf={} data={}",
                                            f.tag(), f.indicator(), f.subfieldId(), f.data());
                                }
                                logger.debug("end");
                            }
                        }

                        @Override
                        public void end() {
                            logger.debug("end object");
                        }

                        @Override
                        public void end(Object info) {
                            logger.debug("end object (info={})", info);
                        }
                    });*/

            InputStream in = InputService.getInputStream(uri);
            InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
            new DNBPICAXmlReader(source).setListener(kv).parse();
            in.close();
            mapper.close();

            logger.info("detected unknown elements = {}",
                    mapper.unknownKeys());

            fileCounter.get().incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while getting next document: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

    private final PicaElementBuilderFactory factory = new PicaElementBuilderFactory() {
        public PicaElementBuilder newBuilder() {
            return new PicaElementBuilder().addOutput(out);
        }
    };

    private final static OurElementOutput out = new OurElementOutput();

    private final static class OurElementOutput extends CountableElementOutput<ResourceContext, Resource> {

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            // set index/type adressing via resource ID
            context.resource().id(IRI.builder().host(index).query(type)
                    .fragment(context.resource().id().getFragment()).build());
            sink.output(context, context.contentBuilder());
            counter.incrementAndGet();
        }

    }

}
