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
package org.xbib.tools.feed.elasticsearch.zdb.bibdat;

import org.xbib.entities.marc.dialects.pica.PicaEntityBuilderState;
import org.xbib.entities.marc.dialects.pica.PicaEntityQueue;
import org.xbib.io.Packet;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.dialects.pica.stream.DNBPicaXmlReader;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.Feeder;
import org.xbib.tools.util.AbstractTarReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public final class BibdatFromOAITar extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(BibdatFromOAITar.class.getSimpleName());

    private static AtomicLong total = new AtomicLong(0L);

    @Override
    public String getName() {
        return "zdb-bibdat-oai-tar";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return BibdatFromOAITar::new;
    }

    @Override
    protected BibdatFromOAITar prepare() throws IOException {
        super.prepare();
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        MyQueue queue = new MyQueue("pica/zdb/bibdat", settings.getAsInt("pipelines", 1));
        queue.setUnmappedKeyListener(key -> {
            if ((settings.getAsBoolean("detect", false))) {
                logger.warn("unmapped field {}", key);
                unmapped.add("\"" + key + "\"");
            }
        });
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC))
                .addListener(queue)
                .addListener(new KeyValueStreamAdapter<FieldList, String>() {
                    @Override
                    public KeyValueStreamAdapter<FieldList, String> keyValue(FieldList key, String value) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("begin");
                            for (Field f : key) {
                                logger.trace("tag={} ind={} subf={} data={}",
                                        f.tag(), f.indicator(), f.subfieldId(), f.data());
                            }
                            logger.trace("end");
                        }
                        return this;
                    }

                });
        final MyTarReader reader = new MyTarReader()
                .setURI(uri)
                .setListener(kv);
        while (reader.hasNext()) {
            reader.next();
        }
        reader.close();
        queue.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown keys = {}", unmapped);
        }
    }

    @Override
    protected BibdatFromOAITar cleanup() {
        super.cleanup();
        logger.info("total={}", total.get());
        return this;
    }

    private class MyTarReader extends AbstractTarReader {

        private final XMLInputFactory factory = XMLInputFactory.newInstance();

        private MarcXchangeListener listener;

        public MyTarReader setURI(URI uri) {
            super.setURI(uri);
            return this;
        }

        public MyTarReader setListener(MarcXchangeListener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        protected void process(Packet packet) throws IOException {
            DNBPicaXmlReader consumer = new DNBPicaXmlReader();
            consumer.setMarcXchangeListener(listener);
            if (logger.isTraceEnabled()) {
                logger.trace("content = {}", packet.toString());
            }
            StringReader sr = new StringReader(packet.toString());
            try {
                XMLEventReader xmlReader = factory.createXMLEventReader(sr);
                while (xmlReader.hasNext()) {
                    consumer.add(xmlReader.nextEvent());
                }
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }
    }

    class MyQueue extends PicaEntityQueue {

        public MyQueue(String path, int workers) {
            super(path, workers);
        }

        @Override
        public void afterCompletion(PicaEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(IRINamespaceContext.getInstance(),
                    settings.get("index"), settings.get("type"));
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getID(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.resource(state.getResource());
        }
    }
}
