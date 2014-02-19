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

import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archivers.tar.TarSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.metrics.MeterMetric;
import org.xbib.pipeline.AbstractPipeline;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineException;
import org.xbib.pipeline.element.LongPipelineElement;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractTarReader<P extends Packet> extends AbstractPipeline<LongPipelineElement, PipelineException> {

    private final Logger logger = LoggerFactory.getLogger(AbstractTarReader.class.getName());

    private final ConnectionService<TarSession> service = ConnectionService.getInstance();

    private final LongPipelineElement counter = new LongPipelineElement().set(new AtomicLong(0L));

    private URI uri;

    private Connection<TarSession> connection;

    private TarSession session;

    private P packet;

    private boolean prepared;

    public AbstractTarReader() {
    }

    public AbstractTarReader setURI(URI uri) {
        this.uri = uri;
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
            process(packet);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected abstract void process(P packet) throws IOException;

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
            return prepared;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private LongPipelineElement nextRead() {
        if (!prepared) {
            return null;
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

    private Packet read(Session session) throws IOException {
        return session.read();
    }
}
