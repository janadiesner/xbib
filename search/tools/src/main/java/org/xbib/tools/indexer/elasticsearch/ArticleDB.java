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
package org.xbib.tools.indexer.elasticsearch;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elasticsearch.support.bulk.transport.MockTransportClientBulk;
import org.xbib.elasticsearch.support.bulk.transport.TransportClientBulk;
import org.xbib.elasticsearch.support.bulk.transport.TransportClientBulkSupport;
import org.xbib.elasticsearch.support.search.transport.TransportClientSearchSupport;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.file.Finder;
import org.xbib.io.file.TextFileConnectionFactory;
import org.xbib.io.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.text.InvalidCharacterException;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;
import org.xbib.xml.XMLUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

/**
 * Index article DB export into Elasticsearch
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ArticleDB extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(ArticleDB.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final static AtomicLong resourceCounter = new AtomicLong(0L);

    private final static AtomicLong errorCounter = new AtomicLong(0L);

    private final static JsonFactory jsonFactory = new JsonFactory();

    private final static TextFileConnectionFactory factory = new TextFileConnectionFactory();

    private static Queue<URI> input;

    private static String index;

    private static String type;

    private ElementOutput output;

    private boolean done = false;

    private Client client;



    public static void main(String[] args) {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.json");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ArticleDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.json)" + lf
                        + " --threads <n>          the number of threads (optional, default: <num-of=cpus)"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();

            logger.info("found {} input files", input);

            URI esURI = URI.create(options.valueOf("elasticsearch").toString());
            index = (String)options.valueOf("index");
            type = (String)options.valueOf("type");
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            boolean mock = (Boolean)options.valueOf("mock");

            final TransportClientBulk es = mock ?
                    new MockTransportClientBulk() :
                    new TransportClientBulkSupport();

            es.maxBulkActions(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .waitForHealthyCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));

            final TransportClientSearchSupport search = new TransportClientSearchSupport()
                    .newClient(esURI);

            logger.info("creating new index ...");
            es.setIndex(index)
                    .setType(type)
                    .dateDetection(false)
                    .newIndex(true); // true = ignore IndexAlreadyExistsException
            logger.info("... new index created");

            logger.info("creating new type ...");
            es.newType();
            logger.info("... new type done");

            final ElasticsearchResourceSink<ResourceContext, Resource> sink =
                    new ElasticsearchResourceSink(es);

            long t0 = System.currentTimeMillis();
            ImportService service = new ImportService()
                    .threads( (Integer) options.valueOf("threads") )
                    .factory(
                            new ImporterFactory() {
                                @Override
                                public Importer newImporter() {
                                    return new ArticleDB(sink, search);
                                }
                            }).execute().shutdown();

            long t1 = System.currentTimeMillis();
            long docs = resourceCounter.get();
            long bytes = es.getVolumeInBytes();
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

            logger.info("Errors: {}", errorCounter);

            service.shutdown();
            es.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    public ArticleDB(ElementOutput output, TransportClientSearchSupport searchSupport) {
        this.output = output;
        this.client = searchSupport.client();
    }

    @Override
    public void close() throws IOException {
        // do not clear input
    }

    @Override
    public boolean hasNext() {
        if (input.isEmpty()) {
            done = true;
        }
        return !done;
    }

    @Override
    public AtomicLong next() {
        if (done) {
            return fileCounter;
        }
        try {
            while (!done) {
                URI uri = input.poll();
                if (uri != null) {
                    push(uri);
                } else {
                    done = true;
                }
                fileCounter.incrementAndGet();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            done = true;
        }
        return fileCounter;
    }

    private void push(URI uri) throws Exception {
        if (uri == null) {
            return;
        }
        InputStream in = factory.getInputStream(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }

        final SimpleResourceContext resourceContext = new SimpleResourceContext();
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("dcterms", "http://purl.org/dc/terms/");
        context.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        context.addNamespace("frbr", "http://purl.org/vocab/frbr/core#");
        context.addNamespace("fabio", "http://purl.org/spar/fabio/");
        context.addNamespace("prism", "http://prismstandard.org/namespaces/basic/2.1/");
        context.addNamespace("bf", "http://bibframe.org/vocab/");
        resourceContext.newNamespaceContext(context);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            JsonParser parser = jsonFactory.createJsonParser(reader);
            JsonToken token = parser.nextToken();
            Resource resource = null;
            String key = null;
            String value = null;
            while (token != null) {
                switch (token) {
                    case START_OBJECT: {
                        resource = resourceContext.newResource();
                        break;
                    }
                    case END_OBJECT: {
                        resourceContext.resource().id(IRI.builder()
                                .scheme("http")
                                .host(index)
                                .query(type)
                                .fragment(resourceContext.resource().id().getFragment())
                                .build());
                        output.output(resourceContext);
                        resourceCounter.incrementAndGet();
                        resource = null;
                        break;
                    }
                    case START_ARRAY: {
                        logger.info("start of file {}", uri);
                        break;
                    }
                    case END_ARRAY: {
                        logger.info("end of file {}", uri);
                        break;
                    }
                    case FIELD_NAME: {
                        key = parser.getCurrentName();
                        break;
                    }
                    case VALUE_STRING:
                    case VALUE_NUMBER_INT:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NULL:
                    case VALUE_TRUE:
                    case VALUE_FALSE: {
                        value = parser.getText();
                        if ("coins".equals(key)) {
                            parseCoinsInto(resource, value);
                        }
                        break;
                    }
                    default:
                        throw new IOException("unknown token: " + token);
                }
                token = parser.nextToken();
            }
        }
    }

    interface URIListener extends URIUtil.ParameterListener {
        void close();
    }

    private void parseCoinsInto(Resource resource, String value) {

        IRI coins = IRI.create("http://localhost/?"
                +  XMLUtil.unescape(value));
        resource
                .add("rdf:type", IRI.create("fabio:Article"))
                .add("rdf:type", IRI.create("frbr:Expression"));
        final Resource r = resource;
        URIListener listener = new URIListener() {

            String aufirst = null;
            String aulast = null;

            String spage = null;
            String epage = null;

            @Override
            public void received(String k, String v) {
                if (v == null) {
                    return;
                }
                v = v.trim();
                if (v.isEmpty()) {
                    return;
                }
                if (v.indexOf('\uFFFD') >= 0) {
                    errorCounter.incrementAndGet();
                }
                switch (k) {
                    case "rft_id" : {
                        if (v.startsWith("info:doi/")) {
                            v = v.substring(9);
                        }
                        try {
                            r.id(IRI.create("http://xbib.info/works#"
                                    + URIUtil.encode(v, "UTF-8")));
                        } catch (UnsupportedEncodingException e) {
                            // not happening
                        }
                        r.add("dcterms:identifier", v)
                                .add("prism:doi", v);
                        break;
                    }
                    case "rft.atitle" : {
                        r.add("dc:title", v);
                        break;
                    }
                    case "rft.jtitle" : {
                        r.add("prism:publicationName", v);
                        break;
                    }
                    case "rft.genre" : {
                        r.add("dc:type", v);
                        break;
                    }
                    case "rft.aulast" : {
                        if (aufirst != null) {
                            r.newResource("foaf:maker")
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst);
                            aulast = null;
                            aufirst = null;
                        } else {
                            aulast = v;
                        }
                        break;
                    }
                    case "rft.aufirst" : {
                        if (aulast != null) {
                            r.newResource("foaf:maker")
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst );
                            aulast = null;
                            aufirst = null;
                        } else {
                            aufirst = v;
                        }
                        break;
                    }
                    case "rft.au" : {
                        r.add("dc:creator", v);
                        break;
                    }
                    case "rft.date" : {
                        Literal l = new SimpleLiteral<>(v).type(Literal.GYEAR);
                        r.add("prism:publicationDate", l);
                        break;
                    }
                    case "rft.volume" : {
                        r.newResource("frbr:embodiment")
                                .add("rdf:type", IRI.create("fabio:PeriodicalVolume"))
                                .add("prism:volume", v);
                        break;
                    }
                    case "rft.issue" : {
                        r.newResource("frbr:embodiment")
                                .add("rdf:type", IRI.create("fabio:PeriodicalIssue"))
                                .add("prism:issueIdentifier", v);
                        break;
                    }
                    case "rft.spage" : {
                        if (spage != null) {
                            r.newResource("frbr:embodiment")
                                    .add("rdf:type", IRI.create("fabio:PrintObject"))
                                    .add("prism:startingPage", spage)
                                    .add("prism:endingPage", epage);
                            spage = null;
                            epage = null;
                        } else {
                            spage = v;
                        }
                        break;
                    }
                    case "rft.epage" : {
                        if (epage != null) {
                            r.newResource("frbr:embodiment")
                                    .add("rdf:type", IRI.create("fabio:PrintObject"))
                                    .add("prism:startingPage", spage)
                                    .add("prism:endingPage", epage);
                            spage = null;
                            epage = null;
                        } else {
                            epage = v;
                        }
                        break;
                    }
                    case "rft_val_fmt":
                    case "ctx_ver":
                    case "rfr_id":
                        break;
                    default: {
                        logger.info("unknown element: {}", k);
                        break;
                    }
                }
            }

            public void close() {
                // pending fields...
                if (aufirst != null || aulast != null) {
                    r.newResource("foaf:maker")
                            .add("rdf:type", IRI.create("foaf:Agent"))
                            .add("foaf:familyName", aulast)
                            .add("foaf:givenName", aufirst);
                }
                if (spage != null || epage != null) {
                    r.newResource("frbr:embodiment")
                            .add("rdf:type", IRI.create("fabio:PrintObject"))
                            .add("prism:startingPage", spage)
                            .add("prism:endingPage", epage);
                }
            }
        };
        try {
            URIUtil.parseQueryString(coins.toURI(), "UTF-8", listener);
        } catch (InvalidCharacterException | UnsupportedEncodingException | URISyntaxException  e) {
            logger.warn("can't parse query string: " + coins, e);
        }
        listener.close();
    }

    private void searchPublication(String title) {
        long millis = 1000;
        QueryBuilder queryBuilder =
                matchPhraseQuery("titleMain", title);
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setQuery(queryBuilder)
                .setSize(10) // size is per shard!
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        searchRequest.setIndices("mix");
        searchRequest.setTypes("works");
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMillis(millis))
                .execute().actionGet();
        long totalHits = searchResponse.getHits().getTotalHits();
        logger.info("searching for {} --> {} hits", title, totalHits);
        while (true) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                logger.info("field keys = {}", hit.getSource().keySet());
            }
        }
    }

}