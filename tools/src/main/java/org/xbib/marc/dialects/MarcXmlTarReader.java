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
package org.xbib.marc.dialects;

import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archivers.tar.TarSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.xml.MarcXmlEventConsumer;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.AbstractPipeline;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineException;
import org.xbib.pipeline.element.LongPipelineElement;
import org.xbib.util.Strings;

public class MarcXmlTarReader<P extends Packet> extends AbstractPipeline<LongPipelineElement, PipelineException> {

    private final Logger logger = LoggerFactory.getLogger(MarcXmlTarReader.class.getName());

    private final XMLInputFactory factory = XMLInputFactory.newInstance();

    private final ConnectionService<TarSession> service = ConnectionService.getInstance();

    private final LongPipelineElement counter = new LongPipelineElement().set(new AtomicLong(0L));

    private URI uri;

    private Iterator<Long> iterator;

    private Connection<TarSession> connection;

    private TarSession session;

    private P packet;

    private boolean prepared;

    private MarcXmlEventConsumer consumer;

    private String clob;

    public MarcXmlTarReader() {
    }

    public MarcXmlTarReader setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    public MarcXmlTarReader setIterator(Iterator<Long> iterator) {
        this.iterator = iterator;
        return this;
    }

    public MarcXmlTarReader setEventConsumer(MarcXmlEventConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public boolean hasNext() {
        try {
            return prepareRead();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public LongPipelineElement next() {
        return nextRead();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void newRequest(Pipeline<MeterMetric, LongPipelineElement> pipeline, LongPipelineElement request) {
        try {
            StringReader sr = new StringReader(clob);
            XMLEventReader xmlReader = factory.createXMLEventReader(sr);
            while (xmlReader.hasNext()) {
                XMLEvent event = xmlReader.nextEvent();
                if (consumer != null) {
                    consumer.add(event);
                }
            }
        } catch (XMLStreamException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void error(Pipeline<MeterMetric, LongPipelineElement> pipeline, LongPipelineElement request, PipelineException error) {
        logger.error(error.getMessage(), error);

    }

    @Override
    public void close() throws IOException {
        if (session != null) {
            session.close();
            logger.info("session closed");
        }
        if (connection != null) {
            connection.close();
            logger.info("connection closed");
        }
    }

    private boolean prepareRead() throws IOException {
        try {
            if (prepared) {
                return true;
            }
            if (session == null) {
                createSession();
            }
            this.packet = (P)read(session);
            this.prepared = packet != null;
            if (prepared) {
                nextByNumber();
                clob = packet.toString();
            }
            return prepared;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private LongPipelineElement nextRead() {
        if (clob == null || clob.length() == 0) {
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("clob={}", clob);
        }
        prepared = false;
        return counter;
    }

    private void createSession() throws IOException {
        this.connection = service
                .getConnectionFactory(uri)
                .getConnection(uri);
        this.session = connection.createSession();
        session.open(Session.Mode.READ);
        if (!session.isOpen()) {
            throw new IOException("session could not be opened");
        }
    }

    /**
     * Like files on ancient magnetic tape. Move forward to the packet we want, compare the number
     * until the desired one.
     *
     * @return the number string of the next record found
     * @throws IOException
     */
    protected String nextByNumber() throws IOException {
        String name = packet.name();
        int pos = name != null ? name.lastIndexOf('/') : -1;
        if (pos < 0 && name != null) {
            return name;
        }
        String numberStr = pos >= 0 ? name.substring(pos + 1) : null;
        while ((pos < 0) || Strings.isNullOrEmpty(numberStr)) {
            logger.warn("skipping packet {}, number does not match", name);
            // next message
            packet = (P)read(session);
            name = packet.name();
            pos = name == null ? -1 : name.lastIndexOf('/');
            numberStr = pos >= 0 ? name.substring(pos + 1) : null;
        }
        return numberStr;
    }

    /**
     *  Read packet, optionally check if iterator gives enough numbers
     *  (assuming iterator counts from 1)
     *
     * @param session session
     * @return
     * @throws IOException
     */
    private Packet read(Session session) throws IOException {
        if (iterator != null) {
            if (iterator.hasNext()) {
                iterator.next();
                return session.read();
            } else {
                throw new EOFException("end of iterator");
            }
        } else {
            return session.read();
        }
    }
}
