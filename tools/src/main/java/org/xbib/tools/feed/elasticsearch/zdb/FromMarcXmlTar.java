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

import org.xbib.elements.marc.MARCContext;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.MarcXmlEventConsumer;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.AbstractResourceContextWriter;
import org.xbib.tools.Feeder;
import org.xbib.tools.util.MarcXmlTarReader;

import java.io.IOException;
import java.net.URI;
import java.text.Normalizer;

/**
 * Elasticsearch indexer for Zeitschriftendatenbank (ZDB) MARC-XML tar archive files
 */
public final class FromMarcXmlTar extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(FromMarcXmlTar.class.getName());

    @Override
    public String getName() {
        return "zdb-marcxmltar-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromMarcXmlTar();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        final MARCElementMapper mapper = new MARCElementMapper(settings.get("elements"))
                .pipelines(settings.getAsInt("pipelines", 1))
                .detectUnknownKeys(settings.getAsBoolean("detect", false))
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        MARCElementBuilder builder = new MARCElementBuilder();
                        builder.addWriter(new MarcContextResourceOutput());
                        return builder;
                    }
                });
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(mapper);
        final MarcXmlEventConsumer consumer = new MarcXmlEventConsumer()
                .setListener(kv);
        final MarcXmlTarReader reader = new MarcXmlTarReader()
                .setURI(uri)
                .setEventConsumer(consumer);
        while (reader.hasNext()) {
            reader.next();
        }
        reader.close();
        mapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown keys={}", mapper.unknownKeys());
        }
    }

    private class MarcContextResourceOutput extends AbstractResourceContextWriter<MARCContext, Resource> {

        @Override
        public void write(MARCContext context) throws IOException {
            IRI id = context.getResource().id();
            context.getResource().id(IRI.builder()
                    .scheme("http")
                    .host(settings.get("index"))
                    .query(settings.get("type"))
                    .fragment(id.getFragment()).build());
            sink.write(context);
        }
    }

}
