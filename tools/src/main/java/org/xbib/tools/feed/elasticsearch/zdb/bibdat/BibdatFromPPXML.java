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

import org.xbib.elements.UnmappedKeyListener;
import org.xbib.elements.marc.dialects.pica.PicaContext;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilder;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilderFactory;
import org.xbib.elements.marc.dialects.pica.PicaElementMapper;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.dialects.pica.DNBPICAXmlReader;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.ContextWriter;
import org.xbib.tools.Feeder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Index bib addresses into Elasticsearch
 */
public final class BibdatFromPPXML extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(BibdatFromPPXML.class.getSimpleName());

    @Override
    public String getName() {
        return "bibdat-ppxml-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new BibdatFromPPXML();
            }
        };
    }

    @Override
    protected BibdatFromPPXML prepare() throws IOException {
        super.prepare();
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bib")
                .pipelines(settings.getAsInt("pipelines", 1))
                .setListener(new UnmappedKeyListener<FieldList>() {
                    @Override
                    public void unknown(FieldList key) {
                        logger.warn("unmapped field {}", key);
                        if ((settings.getAsBoolean("detect", false))) {
                            unmapped.add("\"" + key + "\"");
                        }
                    }
                })
                .start(new PicaElementBuilderFactory() {
                    public PicaElementBuilder newBuilder() {
                        PicaElementBuilder builder = new PicaElementBuilder();
                        builder.addWriter(new PicaContextOutput());
                        return builder;
                    }
                });
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(new StringTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFKC);
                    }
                })
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<FieldList, String>() {
                    @Override
                    public KeyValueStreamAdapter<FieldList, String> begin() {
                        if (logger.isTraceEnabled()) {
                            logger.trace("begin object");
                        }
                        return this;
                    }

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

                    @Override
                    public KeyValueStreamAdapter<FieldList, String> end() {
                        if (logger.isTraceEnabled()) {
                            logger.trace("end object");
                        }
                        return this;
                    }

                });
        InputStream in = InputService.getInputStream(uri);
        new DNBPICAXmlReader().setListener(kv).parse(in);
        in.close();
        mapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("detected unmapped elements = {}", unmapped);
        }
    }

    private class PicaContextOutput implements ContextWriter<PicaContext, Resource> {

        @Override
        public void write(PicaContext context) throws IOException {
            context.getResource().id(IRI.builder().host(settings.get("index")).query(settings.get("type"))
                    .fragment(context.getID()).build());
            sink.write(context);
        }

    }

}
