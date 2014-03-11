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
package org.xbib.tools.elasticsearch.feed.zdb;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.io.Packet;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.dialects.AbstractTarReader;
import org.xbib.marc.dialects.pica.DNBPicaXmlEventConsumer;
import org.xbib.pipeline.PipelineException;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.tools.elasticsearch.Feeder;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.pica.PicaContext;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilder;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilderFactory;
import org.xbib.elements.marc.dialects.pica.PicaElementMapper;
import org.xbib.iri.IRI;
import org.xbib.io.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.xcontent.ContentBuilder;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.text.Normalizer;
import java.util.concurrent.atomic.AtomicLong;

public final class BibdatZDBFromOAITar extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(BibdatZDBFromOAITar.class.getSimpleName());

    private static AtomicLong total = new AtomicLong(0L);

    public static void main(String[] args) {
        try {
            new BibdatZDBFromOAITar()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private BibdatZDBFromOAITar() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new BibdatZDBFromOAITar();
            }
        };
    }

    @Override
    protected BibdatZDBFromOAITar prepare(Ingest output) {
        return this;
    }

    @Override
    protected BibdatZDBFromOAITar prepare() throws IOException {
        super.prepare();
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        logger.info("start of processing {}", uri);

        final OurElementOutput out = new OurElementOutput();
        PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bibdat")
                .pipelines(settings.getAsInt("pipelines", 1))
                .detectUnknownKeys(settings.getAsBoolean("detect", false))
                .start(new PicaElementBuilderFactory() {
                    public PicaElementBuilder newBuilder() {
                        return new PicaElementBuilder().addOutput(out);
                    }
                });
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                    @Override
                    public String transform(String value) { return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                    @Override
                    public KeyValueStreamAdapter<FieldCollection, String> keyValue(FieldCollection key, String value) {
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
        mapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown keys = {}", mapper.unknownKeys());
        }
        total.addAndGet(out.getCounter());
        logger.info("end of processing {}, counter = {}", uri, out.getCounter());
    }

    @Override
    protected BibdatZDBFromOAITar cleanup() {
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
            DNBPicaXmlEventConsumer consumer = new DNBPicaXmlEventConsumer();
            consumer.setListener(listener);
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

        @Override
        public void error(Pipeline pipeline, PipelineRequest request, PipelineException error) {
            logger.error(error.getMessage(), error);
        }
    }

    private class OurElementOutput extends CountableElementOutput<PicaContext, Resource> {

        @Override
        public void output(PicaContext context, ContentBuilder contentBuilder) throws IOException {
            IRI id = IRI.builder()
                    .scheme("http")
                    .host(settings.get("index"))
                    .path("/pica/zdb/bibdat") //ignored
                    .query(settings.get("type"))
                    .fragment(context.getID()).build();
            context.resource().id(id);
            sink.output(context, context.contentBuilder());
            counter.incrementAndGet();
        }
    }

}
