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
package org.xbib.tools.feed.elasticsearch.zdb;

import org.xbib.elements.UnmappedKeyListener;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.Packet;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.DataField;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.marc.xml.stream.MarcXchangeReader;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.context.ResourceContextWriter;
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

/**
 * Index SRU data
 */
public class FromSRUTar extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(FromSRUTar.class.getName());

    @Override
    public String getName() {
        return "zdb-srutar-elasticsearch";
    }

    protected FromSRUTar prepare() throws IOException {
        super.prepare();
        return this;
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromSRUTar();
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {

        final String bibIndex = settings.get("bibIndex", "zdb");
        final String bibType = settings.get("bibType", "title");
        final String holIndex = settings.get("holIndex", "zdbholdings");
        final String holType = settings.get("holType", "holdings");

        final OurContextResourceOutput bibout = new OurContextResourceOutput().setIndex(bibIndex).setType(bibType);

        final OurContextResourceOutput holout = new OurContextResourceOutput().setIndex(holIndex).setType(holType);

        final Set<String> unmappedbib = Collections.synchronizedSet(new TreeSet<String>());
        final MARCElementMapper bibmapper = new MARCElementMapper("marc/zdb/bib")
                .pipelines(settings.getAsInt("pipelines", 1))
                .setListener(new UnmappedKeyListener<DataField>() {
                    @Override
                    public void unknown(DataField key) {
                        logger.warn("unmapped field {}", key.toSpec());
                        if ((settings.getAsBoolean("detect", false))) {
                            unmappedbib.add("\"" + key.toSpec() + "\"");
                        }
                    }
                })
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        MARCElementBuilder builder = new MARCElementBuilder();
                        builder.addWriter(bibout);
                        return builder;
                    }
                });

        final Set<String> unmappedhol = Collections.synchronizedSet(new TreeSet<String>());
        final MARCElementMapper holmapper = new MARCElementMapper("marc/zdb/hol")
                .pipelines(settings.getAsInt("pipelines", 1))
                .setListener(new UnmappedKeyListener<DataField>() {
                    @Override
                    public void unknown(DataField key) {
                        logger.warn("unmapped field {}", key.toSpec());
                        if ((settings.getAsBoolean("detect", false))) {
                            unmappedbib.add("\"" + key.toSpec() + "\"");
                        }
                    }
                })
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        MARCElementBuilder builder = new MARCElementBuilder();
                        builder.addWriter(holout);
                        return builder;
                    }
                });

        final MarcXchange2KeyValue bib = new MarcXchange2KeyValue()
                .setStringTransformer(new StringTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(bibmapper);

        final MarcXchange2KeyValue hol = new MarcXchange2KeyValue()
                .setStringTransformer(new StringTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(holmapper);

        final MarcXchangeContentHandler handler = new MarcXchangeContentHandler()
                .addListener("Bibliographic", bib)
                .addListener("Holdings", hol);

        final MyTarReader reader = new MyTarReader()
                .setURI(uri)
                .setListener(handler);
        while (reader.hasNext()) {
            reader.next();
        }
        reader.close();
        bibmapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown bib keys={}", unmappedbib);
        }
        holmapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown hol keys={}", unmappedhol);
        }
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
            MarcXchangeReader consumer = new MarcXchangeReader();
            consumer.setMarcXchangeListener(listener);
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

    private class OurContextResourceOutput implements ResourceContextWriter<ResourceContext<Resource>, Resource> {

        String index;

        String type;

        public OurContextResourceOutput setIndex(String index) {
            this.index = index;
            return this;
        }

        public OurContextResourceOutput setType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public void write(ResourceContext context) throws IOException {
            IRI iri = context.getResource().id();
            context.getResource().id(IRI.builder().scheme("http").host(index).query(type).fragment(iri.getFragment()).build());
            sink.write(context);
        }
    }

}
