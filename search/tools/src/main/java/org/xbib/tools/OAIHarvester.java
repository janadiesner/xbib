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

import org.xbib.io.Connection;
import org.xbib.io.NullWriter;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archivers.TarConnectionFactory;
import org.xbib.io.archivers.TarSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.record.ListRecordsRequest;
import org.xbib.oai.record.ListRecordsResponseListener;
import org.xbib.oai.xml.MetadataHandler;
import org.xbib.oai.rdf.RdfMetadataHandler;
import org.xbib.oai.rdf.RdfOutput;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.oai.xml.XmlMetadataHandler;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.util.DateUtil;
import org.xbib.util.URIUtil;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.text.Normalizer;
import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Queues.newConcurrentLinkedQueue;

/**
 * Harvest from OAI
 */
public abstract class OAIHarvester extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(OAIHarvester.class.getSimpleName());

    protected final static SimpleResourceContext resourceContext = new SimpleResourceContext();

    protected static TarSession session;

    @Override
    protected OAIHarvester prepare() {
        String[] inputs = settings.getAsArray("input");
        if (inputs == null) {
            throw new IllegalArgumentException("no input given");
        }
        this.input = newConcurrentLinkedQueue();
        for (String uri : inputs) {
            this.input.offer(URI.create(uri));
        }
        String output = settings.get("output");
        try {
            TarConnectionFactory factory = new TarConnectionFactory();
            Connection<TarSession> connection = factory.getConnection(URI.create("targz:" + output));
            session = connection.createSession();
            session.open(Session.Mode.WRITE);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    public OAIHarvester run() throws Exception {
        super.run();
        session.close();
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        Map<String,String> params = URIUtil.parseQueryString(uri);
        String server = uri.toString();
        String prefix = params.get("prefix");
        String set = params.get("set");
        Date from = DateUtil.parseDateISO(params.get("from"));
        Date until = DateUtil.parseDateISO(params.get("until"));
        final OAIClient client = OAIClientFactory.newClient(server);
        ListRecordsRequest request = client.newListRecordsRequest()
                .setMetadataPrefix(prefix)
                .setSet(set)
                .setFrom(from)
                .setUntil(until);
        do {
            try {
                ListRecordsResponseListener listener = new ListRecordsResponseListener(request);
                if ("xml".equals(settings.get("handler"))) {
                    listener.register(xmlMetadataHandler());
                } else if ("turtle".equals(settings.get("handler"))) {
                    listener.register(turtleMetadataHandler());
                } else if ("ntriples".equals(settings.get("handler"))) {
                    listener.register(ntripleMetadataHandler());
                }
                request.prepare().execute(listener).waitFor();
                if (listener.isFailure()) {
                    logger.error("request failed");
                    request = null;
                } else if (listener.getResponse() != null) {
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

    protected MetadataHandler xmlMetadataHandler() {
        return new XmlPacketHandler().setWriter(new StringWriter());
    }

    protected MetadataHandler turtleMetadataHandler() {
        final RdfMetadataHandler metadataHandler = new RdfMetadataHandler();
        final RdfResourceHandler resourceHandler = rdfResourceHandler();
        final RdfOutput rdfout = new TurtleOutput(metadataHandler.getContext());
        metadataHandler.setHandler(resourceHandler)
                .setOutput(rdfout);
        return metadataHandler;
    }
    protected MetadataHandler ntripleMetadataHandler() {
        final RdfMetadataHandler metadataHandler = new RdfMetadataHandler();
        final RdfResourceHandler resourceHandler = rdfResourceHandler();
        final RdfOutput rdfout = new NTripleOutput();
        metadataHandler.setHandler(resourceHandler)
                .setOutput(rdfout);
        return metadataHandler;
    }

    protected RdfResourceHandler rdfResourceHandler() {
        return new RdfResourceHandler(resourceContext);
    }

    protected class XmlPacketHandler extends XmlMetadataHandler {

        public void endDocument() throws SAXException {
            super.endDocument();
            logger.info("got XML document {}", getIdentifier());
            try {
                Packet p = session.newPacket();
                p.name(getIdentifier());
                String s = getWriter().toString();
                // for Unicode in non-canonical form, normalize it here
                s = Normalizer.normalize(s, Normalizer.Form.NFC);
                p.packet(s);
                session.write(p);
            } catch (IOException e) {
                throw new SAXException(e);
            }
            setWriter(new StringWriter());
        }
    }

    protected class TurtleOutput extends RdfOutput {

        StringWriter sw;

        TurtleWriter writer;

        TurtleOutput(IRINamespaceContext context) {
            try {
                this.writer = new TurtleWriter()
                        .output(sw)
                        .setContext(context)
                        .writeNamespaces();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public RdfOutput output(ResourceContext resourceContext) throws IOException {
            writer.write(resourceContext.resource());
            Packet p = session.newPacket();
            p.name(resourceContext.resource().id().getASCIIAuthority());
            String s = sw.toString();
            // for Unicode in non-canonical form, normalize it here
            s = Normalizer.normalize(s, Normalizer.Form.NFC);
            p.packet(s);
            session.write(p);
            sw = new StringWriter();
            writer.output(sw);
            return this;
        }
    }

    protected class NTripleOutput extends RdfOutput {

        StringWriter sw;

        NTripleWriter writer;

        NTripleOutput() {
            this.writer = new NTripleWriter()
                    .output(sw);
        }

        @Override
        public RdfOutput output(ResourceContext resourceContext) throws IOException {
            writer.write(resourceContext.resource());
            Packet p = session.newPacket();
            p.name(resourceContext.resource().id().getASCIIAuthority());
            String s = sw.toString();
            // for Unicode in non-canonical form, normalize it here
            s = Normalizer.normalize(s, Normalizer.Form.NFC);
            p.packet(s);
            session.write(p);
            sw = new StringWriter();
            writer.output(sw);
            return this;
        }
    }
}
