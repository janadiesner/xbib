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
import java.net.URI;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.namespace.QName;

import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.ResourceSink;
import org.xbib.elasticsearch.support.client.IngestClient;
import org.xbib.elasticsearch.support.client.MockIngestClient;
import org.xbib.elements.ElementOutput;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.AbstractPipeline;
import org.xbib.pipeline.SimplePipelineExecutor;
import org.xbib.pipeline.Pipeline;
import org.xbib.io.InputService;
import org.xbib.io.file.Finder;
import org.xbib.pipeline.element.CounterElement;
import org.xbib.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.options.OptionParser;
import org.xbib.options.OptionSet;
import org.xbib.util.FormatUtil;
import org.xml.sax.SAXException;

/**
 * Elasticsearch indexer for ELektronische Zeitschriftenbibliothek (EZB)
 *
 * Format-Dokumentation
 * http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf
 *
 */
public final class EZB extends AbstractPipeline<CounterElement> {

    private final static Logger logger = LoggerFactory.getLogger(EZB.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static CounterElement fileCounter = new CounterElement().set(new AtomicLong(0L));

    private static Queue<URI> input;

    private static String index;

    private static String type;

    private final SimpleResourceContext resourceContext = new SimpleResourceContext();

    private ElementOutput out;

    private boolean done = false;


    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("shards").withRequiredArg().ofType(Integer.class).defaultsTo(3);
                    accepts("replica").withRequiredArg().ofType(Integer.class).defaultsTo(0);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.xml");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                }
            };
            OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + EZB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --shards <n>           Elasticsearch number of shards" + lf
                        + " --replica <n>          Elasticsearch number of replica shard level" + lf
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString())
                    .find(options.valueOf("path").toString())
                    .chronologicallySorted()
                    .getURIs();

            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {}, threads = {}", input, threads);

            URI esURI = URI.create(options.valueOf("elasticsearch").toString());
            index = options.valueOf("index").toString();
            type = options.valueOf("type").toString();
            Integer shards = (Integer)options.valueOf("shards");
            Integer replica = (Integer)options.valueOf("replica");
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            boolean mock = (Boolean) options.valueOf("mock");

            final IngestClient es = mock ? new MockIngestClient() : new IngestClient();

            es.maxActionsPerBulkRequest(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .setIndex(index)
                    .setType(type)
                    .waitForCluster();
            // do not delete index
            es.dateDetection(false)
                    .shards(shards)
                    .replica(replica)
                    .newIndex();

            final ResourceSink<ResourceContext, Resource> sink =
                    new ResourceSink(es);

            long t0 = System.currentTimeMillis();
            SimplePipelineExecutor service = new SimplePipelineExecutor().concurrency(threads).provider(
                    new PipelineProvider() {
                        @Override
                        public Pipeline get() {
                            return new EZB(sink);
                        }
                    })
                    .execute()
                    .waitFor();

            // compute statistics
            long t1 = System.currentTimeMillis();
            long docs = sink.getCounter();
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

    public EZB(ElementOutput out) {
        this.out = out;
    }

    @Override
    public void close() throws IOException {
        input.clear();
    }

    @Override
    public boolean hasNext() {
        return !done && !input.isEmpty();
    }

    private String DEFAULT_NS = "http://ezb.uni-regensburg.de/ezeit/";

    private String DEFAULT_NS_URI = "ezb";

    @Override
    public CounterElement next() {
        URI uri = input.poll();
        done = uri == null;
        if (done) {
            return fileCounter;
        }
        try {
            AbstractXmlHandler handler = new Handler(resourceContext)
                    .setListener(new ResourceBuilder())
                    .setDefaultNamespace(DEFAULT_NS_URI, DEFAULT_NS);
            InputStream in = InputService.getInputStream(uri);
            new XmlReader()
                    .setNamespaces(false)
                    .setHandler(handler)
                    .parse(in);
            in.close();
            fileCounter.get().incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while getting next document: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

    class Handler extends AbstractXmlResourceHandler {

        public Handler(ResourceContext ctx) {
            super(ctx);
        }

        @Override
        public void endElement (String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("license_entry_id".equals(name.getLocalPart()) && identifier == null) {
                IRI id = IRI.builder().scheme("iri").host(index).query(type).fragment(value).build();
                resourceContext.resource().id(id);
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "license_set".equals(name.getLocalPart());
        }

        @Override
        public void closeResource() {
            try { 
                out.output(resourceContext, resourceContext.contentBuilder());
            } catch (IOException e ) {
                logger.error(e.getMessage(), e);
            }
            super.closeResource();
        }

        @Override
        public boolean skip(QName name) {
            return "ezb-export".equals(name.getLocalPart())
            || "release".equals(name.getLocalPart())
            || "version".equals(name.getLocalPart())
            || name.getLocalPart().startsWith("@");
        }
        
        @Override
        public Object toObject(QName name, String content) {
            switch (name.getLocalPart()) {
                case "reference_url":
                    // fall-through
                case "readme_url":
                    return URIUtil.decode(content, Charset.forName("UTF-8"));
                case "zdbid": {
                    return content.replaceAll("\\-", "").toLowerCase();
                }
                case "type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1: return "full-text-online"; //"Volltext nur online";
                        case 2: return "full-text-online-and-print"; //"Volltext online und Druckausgabe";
                        case 9: return "local"; //"lokale Zeitschrift";
                        case 11: return "digitized"; //"retrodigitalisiert";
                        default: throw new IllegalArgumentException("unknown type_id: " + content);
                    }
                }
                case "license_type_id" : {
                    switch (Integer.parseInt(content)) {
                        case 1 : return "local-license"; // "Einzellizenz";
                        case 2 : return "consortia-license"; //"Konsortiallizenz";
                        case 4 : return "supra-regional-license"; // "Nationallizenz";
                        default: throw new IllegalArgumentException("unknown license_type_id: " + content);
                    }
                }
                case "price_type_id" : {
                    switch (Integer.parseInt(content)) {
                        case 1 : return "no-fee"; //"lizenzfrei";
                        case 2 : return "no-fee-included-in-print"; //"Kostenlos mit Druckausgabe";
                        case 3 : return "fee"; //"Kostenpflichtig";
                        default: throw new IllegalArgumentException("unknown price_type_id: " + content);
                    }
                }
                case "ill_code" : {
                    switch (content) {
                        case "n" : return "no"; // "nein";
                        case "l" : return "copy-loan"; //"ja, Leihe und Kopie";
                        case "k" : return "copy"; //"ja, nur Kopie";
                        case "e" : return "copy-electronic";  //"ja, auch elektronischer Versand an Nutzer";
                        case "ln" : return "copy-loan-domestic";  //"ja, Leihe und Kopie (nur Inland)";
                        case "kn" : return "copy-domestic";  //"ja, nur Kopie (nur Inland)";
                        case "en" : return "copy-electronic-domestic";  //"ja, auch elektronischer Versand an Nutzer (nur Inland)";
                        default: throw new IllegalArgumentException("unknown ill_code: " + content);
                    }
                }
            }
            return super.toObject(name, content);
        }
    }

    class ResourceBuilder implements TripleListener {

        @Override
        public TripleListener startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public TripleListener endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public ResourceBuilder newIdentifier(IRI identifier) {
            resourceContext.resource().id(identifier);
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            resourceContext.resource().add(triple);
            return this;
        }
    }

}
