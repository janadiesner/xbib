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
package org.xbib.tools.elasticsearch.feed.articles;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.tools.elasticsearch.Feeder;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.io.InputService;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.Pipeline;
import org.xbib.io.archivers.file.Finder;
import org.xbib.tools.articles.SerialsDB;
import org.xbib.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RDF;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.text.InvalidCharacterException;
import org.xbib.util.Entities;
import org.xbib.xml.XMLUtil;

/**
 * Index article DB into Elasticsearch
 */
public class JsonCoins extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(JsonCoins.class.getSimpleName());

    private final static IRINamespaceContext context = IRINamespaceContext.newInstance();

    static {
        context.addNamespace(RDF.NS_PREFIX, RDF.NS_URI);
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("dcterms", "http://purl.org/dc/terms/");
        context.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        context.addNamespace("frbr", "http://purl.org/vocab/frbr/core#");
        context.addNamespace("fabio", "http://purl.org/spar/fabio/");
        context.addNamespace("prism", "http://prismstandard.org/namespaces/basic/2.1/");
    }

    private final static JsonFactory jsonFactory = new JsonFactory();

    private final static AtomicLong resourceCounter = new AtomicLong(0L);

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final static SerialsDB serialsdb = new SerialsDB();

    private final static AtomicLong counter = new AtomicLong(0L);

    public static void main(String[] args) {
        try {
            new JsonCoins()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private JsonCoins() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new JsonCoins();
            }
        };
    }

    @Override
    protected JsonCoins prepare() throws IOException {
        try {
            Queue<URI> input = new Finder(settings.get("serials"))
                    .find(settings.get("path"))
                    .getURIs();
            logger.info("parsing initial set of serials...");
            try {
                serialsdb.run(settings, input);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            logger.info("serials done, getSize = {}", serialsdb.getMap().size());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        super.prepare();
        return this;
    }

    @Override
    public JsonCoins prepare(Ingest output) {
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        String index = settings.get("index");
        String type = settings.get("type");
        if (serialsdb.getMap().isEmpty()) {
            throw new IllegalArgumentException("no serials?");
        }
        InputStream in = InputService.getInputStream(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }
        final SimpleResourceContext resourceContext = new SimpleResourceContext();
        resourceContext.newNamespaceContext(context);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF8))) {
            JsonParser parser = jsonFactory.createParser(reader);
            JsonToken token = parser.nextToken();
            Resource resource = null;
            String key = null;
            String value;
            Result result = Result.OK;
            while (token != null) {
                switch (token) {
                    case START_OBJECT: {
                        resource = resourceContext.newResource();
                        break;
                    }
                    case END_OBJECT: {
                        String indexType = type;
                        switch (result) {
                            case OK:
                                indexType = type;
                                break;
                            case MISSINGSERIAL:
                                indexType = type + "noserials";
                                break;
                            case ERROR:
                                indexType = type + "errors";
                                break;
                        }
                        resourceContext.resource().id(IRI.builder()
                                .scheme("http")
                                .host(index)
                                .query(indexType)
                                .fragment(resourceContext.resource().id().getFragment())
                                .build());
                        sink.output(resourceContext, resourceContext.contentBuilder());
                        resourceCounter.incrementAndGet();
                        resource = null;
                        break;
                    }
                    case START_ARRAY: {
                        break;
                    }
                    case END_ARRAY: {
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
                            result = parseCoinsInto(resource, value);
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

    protected interface URIListener extends URIUtil.ParameterListener {

        void close();

        boolean hasErrors();

        boolean missingSerial();
    }

    private final IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private final IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    private final IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    private final IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    private final IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    private final IRI FOAF_MAKER = IRI.create("foaf:maker");

    private final IRI FOAF_AGENT = IRI.create("foaf:agent");

    private final IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    private final IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

    protected enum Result {
        OK, ERROR, MISSINGSERIAL
    }

    protected Result parseCoinsInto(Resource resource, String value) {
        IRI coins = IRI.builder()
                .scheme("http")
                .host("localhost")
                .query(XMLUtil.unescape(value))
                .build();
        final Resource r = resource;
        URIListener listener = new URIListener() {
            boolean error = false;
            boolean missingserial = false;

            String aufirst = null;
            String aulast = null;

            String spage = null;
            String epage = null;

            Resource j = null;
            String title = null;
            Collection<Node> issns = null;
            String year = null;

            String author = null;
            String work = null;

            @Override
            public void received(String k, String v) {
                if (v == null) {
                    return;
                }
                v = v.trim();
                if (v.isEmpty()) {
                    return;
                }
                if (v.indexOf('\uFFFD') >= 0) { // Unicode replacement character
                    error = true;
                }
                switch (k) {
                    case "rft_id" : {
                        String w = URIUtil.decode(v, UTF8).toLowerCase();
                        if (w.startsWith("info:doi/")) {
                            w = w.substring(9);
                        } else if (w.startsWith("http://dx.doi.org/")) {
                            w = w.substring(18);
                        }
                        try {
                            // DOI is case-insensitive
                            // encode as URI, but info URI RFC wants slash as unencoded character
                            String s = URIUtil.encode(w, UTF8);
                            s = s.replaceAll("%2F","/");
                            IRI iri = IRI.builder()
                                    .scheme("http")
                                    .host("xbib.info")
                                    .path("/doi/")
                                    .fragment(s)
                                    .build();
                            r.id(iri)
                                .a(FABIO_ARTICLE)
                                .add("prism:doi", w);
                        } catch (Exception e) {
                            logger.warn("can't build IRI from DOI " + v, e);
                        }
                        break;
                    }
                    case "rft.atitle" : {
                        v = Entities.HTML40.unescape(v);
                        r.add("dc:title", v);
                        work = v;
                        break;
                    }
                    case "rft.jtitle" : {
                        v = Entities.HTML40.unescape(v);
                        title = v;
                        j = r.newResource(FRBR_PARTOF)
                                .a(FABIO_JOURNAL)
                                .add("prism:publicationName", v);
                        Resource serial = serialsdb.getMap().get(v);
                        if (serial != null) {
                            issns = serial.objects("prism:issn");
                            if (issns != null) {
                                for (Node issn : issns) {
                                    j.add("prism:issn", issn.toString());
                                }
                            }
                            Node publisher = serial.literal("dc:publisher");
                            if (publisher != null) {
                                j.add("dc:publisher", publisher.toString() );
                            }
                        } else {
                            missingserial = true;
                        }
                        break;
                    }
                    case "rft.aulast" : {
                        v = Entities.HTML40.unescape(v);
                        if (aulast != null) {
                            r.newResource(FOAF_MAKER)
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst );
                            author = aulast + " " + aufirst;
                            aulast = null;
                            aufirst = null;
                        } else {
                            aulast = v;
                        }
                        break;
                    }
                    case "rft.aufirst" : {
                        v = Entities.HTML40.unescape(v);
                        if (aufirst != null) {
                            r.newResource(FOAF_MAKER)
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst);
                            author = aulast + " " + aufirst;
                            aulast = null;
                            aufirst = null;
                        } else {
                            aufirst = v;
                        }
                        break;
                    }
                    case "rft.au" : {
                        // fix author strings
                        if ("&NA;".equals(v)) {
                            v = null;
                        } else {
                            v = Entities.HTML40.unescape(v);
                        }
                        r.add("dc:creator", v);
                        if (author == null) {
                            author = v;
                        }
                        break;
                    }
                    case "rft.date" : {
                        year = v;
                        Literal l = new SimpleLiteral(v).type(Literal.GYEAR);
                        r.add("prism:publicationDate", l);
                        break;
                    }
                    case "rft.volume" : {
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_VOLUME)
                                .add("prism:volume", v);
                        break;
                    }
                    case "rft.issue" : {
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_ISSUE)
                                .add("prism:number", v);
                        break;
                    }
                    case "rft.spage" : {
                        if (spage != null) {
                            r.newResource(FRBR_EMBODIMENT)
                                    .a(FABIO_PRINT_OBJECT)
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
                            r.newResource(FRBR_EMBODIMENT)
                                    .a(FABIO_PRINT_OBJECT)
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
                    case "rft.genre":
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
                    r.newResource(FOAF_MAKER)
                            .a(FOAF_AGENT)
                            .add("foaf:familyName", aulast)
                            .add("foaf:givenName", aufirst);
                    author = aulast + " " + aufirst;
                }
                if (spage != null || epage != null) {
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add("prism:startingPage", spage)
                            .add("prism:endingPage", epage);
                }
                // create bibliographic key
                String key = new WorkAuthor()
                        .authorName(author)
                        .workName(work)
                        .createIdentifier();
                // move aulast to author if no author
                if (author == null && aulast != null) {
                    author = aulast;
                }
                if (author!= null && work != null && key != null) {
                    r.add("xbib:key", key);
                }
            }

            public boolean hasErrors() {
                return error;
            }

            public boolean missingSerial() {
                return missingserial;
            }
        };
        try {
            URIUtil.parseQueryString(coins.toURI(), UTF8, listener);
        } catch (InvalidCharacterException | URISyntaxException  e) {
            logger.warn("can't parse query string: " + coins, e);
        }
        listener.close();
        return listener.hasErrors() ? Result.ERROR :
               listener.missingSerial() ? Result.MISSINGSERIAL :
               Result.OK;
    }

}
