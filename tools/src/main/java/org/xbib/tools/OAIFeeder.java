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
package org.xbib.tools;

import org.xbib.io.NullWriter;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.listrecords.ListRecordsListener;
import org.xbib.oai.listrecords.ListRecordsRequest;
import org.xbib.oai.rdf.RdfMetadataHandler;
import org.xbib.oai.rdf.RdfOutput;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.oai.xml.MetadataHandler;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.util.DateUtil;
import org.xbib.util.URIUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Queues.newConcurrentLinkedQueue;

/**
 * Harvest from OAI and feed to Elasticsearch
 */
public abstract class OAIFeeder extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(OAIFeeder.class.getSimpleName());

    @Override
    protected OAIFeeder prepare() {
        String[] inputs = settings.getAsArray("input");
        if (inputs == null) {
            throw new IllegalArgumentException("no input given");
        }
        input = newConcurrentLinkedQueue();
        for (String uri : inputs) {
            input.offer(URI.create(uri));
        }
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        Map<String,String> params = URIUtil.parseQueryString(uri);
        String server = uri.toString();
        String metadataPrefix = params.get("metadataPrefix");
        String set = params.get("set");
        Date from = DateUtil.parseDateISO(params.get("from"));
        Date until = DateUtil.parseDateISO(params.get("until"));
        final OAIClient client = OAIClientFactory.newClient(server);
        client.setTimeout(settings.getAsInt("timeout", 60000));
        ListRecordsRequest request = client.newListRecordsRequest()
                .setMetadataPrefix(metadataPrefix)
                .setSet(set)
                .setFrom(from)
                .setUntil(until);
        do {
            try {
                // TODO RDF or XML?
                SimpleResourceContext resourceContext = new SimpleResourceContext();
                RdfMetadataHandler metadataHandler = new RdfMetadataHandler()
                        .setHandler(new RdfResourceHandler(resourceContext))
                        .setOutput(new ElasticOut());
                request.addHandler(metadataHandler);

                ListRecordsListener listener = new ListRecordsListener(request);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    NullWriter w = new NullWriter();
                    listener.getResponse().to(w);
                    request = client.resume(request, listener.getResumptionToken());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                request = null;
            }
        } while (request != null);
        client.close();
    }


    private class XmlMetadataHandler extends MetadataHandler {

        final XmlHandler handler;

        final IRINamespaceContext context;

        final ResourceContext resourceContext;

        XmlMetadataHandler(XmlHandler handler, ResourceContext resourceContext) {
            this.handler = handler;
            this.resourceContext = resourceContext;
            context = IRINamespaceContext.newInstance();
            context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            resourceContext.setNamespaceContext(context);
        }

        @Override
        public void startDocument() throws SAXException {
            handler.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            handler.endDocument();
            String identifier = getHeader().getIdentifier();
            try {
                IRI iri = IRI.builder().scheme("http")
                        .host(settings.get("index"))
                        .query(settings.get("type"))
                        .fragment(identifier).build();
                resourceContext.getResource().id(iri);
                sink.output(resourceContext, resourceContext.getResource(), resourceContext.getContentBuilder());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void startPrefixMapping(String string, String string1) throws SAXException {
            handler.startPrefixMapping(string, string1);
        }

        @Override
        public void endPrefixMapping(String string) throws SAXException {
            handler.endPrefixMapping(string);
        }

        @Override
        public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
            handler.startElement(ns, localname, string2, atrbts);
        }

        @Override
        public void endElement(String ns, String localname, String string2) throws SAXException {
            handler.endElement(ns, localname, string2);
        }

        @Override
        public void characters(char[] chars, int i, int i1) throws SAXException {
            handler.characters(chars, i, i1);
        }
    }

    private class ResourceBuilder implements TripleListener {

        ResourceContext resourceContext;

        ResourceBuilder(ResourceContext resourceContext) {
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
        public ResourceBuilder newIdentifier(IRI identifier) {
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            resourceContext.getResource().add(triple);
            return this;
        }

        @Override
        public TripleListener end() {
            return this;
        }
    }

    class ElasticOut extends RdfOutput {
        @Override
        public RdfOutput output(ResourceContext resourceContext) throws IOException {
            sink.output(resourceContext, resourceContext.getResource(), resourceContext.getContentBuilder());
            return this;
        }
    }

}
