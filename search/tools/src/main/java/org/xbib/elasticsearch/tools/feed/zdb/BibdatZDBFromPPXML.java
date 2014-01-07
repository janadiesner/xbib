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
package org.xbib.elasticsearch.tools.feed.zdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.text.Normalizer;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.tools.Feeder;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.pica.PicaContext;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilder;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilderFactory;
import org.xbib.elements.marc.dialects.pica.PicaElementMapper;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.io.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.DNBPICAXmlReader;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xml.sax.InputSource;

/**
 * Index bib addresses into Elasticsearch
 */
public final class BibdatZDBFromPPXML extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(BibdatZDBFromPPXML.class.getSimpleName());

    public static void main(String[] args) {
        try {
            new BibdatZDBFromPPXML()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private BibdatZDBFromPPXML() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new BibdatZDBFromPPXML();
            }
        };
    }

    @Override
    protected BibdatZDBFromPPXML prepare(Ingest output) {
        return this;
    }

    @Override
    protected BibdatZDBFromPPXML prepare() throws IOException {
        super.prepare();
        logger.info("got settings {}", settings.getAsMap());
        out.setIndex(settings.get("index"));
        out.setType(settings.get("type"));
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bib")
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
                    public String transform(String value) {
                        return Normalizer.normalize(value,Normalizer.Form.NFKC);
                    }
                })
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                    @Override
                    public void begin() {
                        if (logger.isTraceEnabled()) {
                            logger.trace("begin object");
                        }
                    }

                    @Override
                    public void keyValue(FieldCollection key, String value) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("begin");
                            for (Field f : key) {
                                logger.trace("tag={} ind={} subf={} data={}",
                                        f.tag(), f.indicator(), f.subfieldId(), f.data());
                            }
                            logger.trace("end");
                        }
                    }

                    @Override
                    public void end() {
                        if (logger.isTraceEnabled()) {
                            logger.trace("end object");
                        }
                    }

                });
        InputStream in = InputService.getInputStream(uri);
        InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
        new DNBPICAXmlReader(source).setListener(kv).parse();
        in.close();
        mapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("detected unknown elements = {}", mapper.unknownKeys());
        }
    }

    private final static OurElementOutput out = new OurElementOutput();

    private final static class OurElementOutput extends CountableElementOutput<PicaContext, Resource> {

        String index;
        String type;

        public void setIndex(String index) {
            this.index = index;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public void output(PicaContext context, ContentBuilder contentBuilder) throws IOException {
            context.resource().id(IRI.builder().host(index).query(type)
                    .fragment(context.getID()).build());
            sink.output(context, context.contentBuilder());
            counter.incrementAndGet();
        }

    }

}
