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
package org.xbib.tools.convert.zdb;

import org.xbib.elements.UnmappedKeyListener;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.DataField;
import org.xbib.marc.Field;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.AbstractResourceContextWriter;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.tools.Converter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * COnverting Zeitschriftendatenbank (ZDB) MARC ISO2709 files
 */
public final class FromMARC extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(FromMARC.class.getName());

    private final Charset UTF8 = Charset.forName("UTF-8");

    private final Charset ISO88591 = Charset.forName("ISO-8859-1");

    @Override
    public String getName() {
        return "zdb-marc-ntriple";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromMARC();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {

        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        final MARCElementMapper mapper = new MARCElementMapper(settings.get("elements"))
                .pipelines(settings.getAsInt("pipelines", 1))
                .setListener(new UnmappedKeyListener<DataField>() {
                    @Override
                    public void unknown(DataField key) {
                        logger.warn("unmapped field {}", key.toSpec());
                        if ((settings.getAsBoolean("detect", false))) {
                            unmapped.add("\"" + key.toSpec() + "\"");
                        }
                    }
                })
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        MARCElementBuilder builder = new MARCElementBuilder();
                        builder.addWriter(new MarcContextResourceOutput());
                        return builder;
                    }
                });

        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(new StringTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(new String(value.getBytes(ISO88591), UTF8),
                                Normalizer.Form.NFKC);
                    }
                })
                .addListener(mapper)
                .addListener(new LoggingAdapter());

        final Iso2709Reader reader = new Iso2709Reader()
                .setMarcXchangeListener(kv);
        // setting the properties is just informational and not used for any purpose.
        reader.setProperty(Iso2709Reader.FORMAT, "MARC21");
        reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
        if ("marc/hol".equals(settings.get("elements")) || "marc/zdb/hol".equals(settings.get("elements"))) {
            reader.setProperty(Iso2709Reader.TYPE, "Holdings");
        }
        reader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
        reader.setProperty(Iso2709Reader.SILENT_ERRORS, true);
        InputStreamReader r = new InputStreamReader(InputService.getInputStream(uri), ISO88591);
        reader.parse(r);
        r.close();
        mapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown keys={}", unmapped);
        }
    }

    private class MarcContextResourceOutput extends AbstractResourceContextWriter<ResourceContext<Resource>, Resource> {

        @Override
        public void write(ResourceContext context) throws IOException {
            IRI iri = context.getResource().id();
            context.getResource().id(IRI.builder().scheme("http").host(settings.get("index")).query(settings.get("type"))
                    .fragment(iri.getFragment()).build());

            StringWriter sw = new StringWriter();
            NTripleWriter writer = new NTripleWriter(sw);
            writer.write(context);
            logger.debug("{}", sw.toString());
        }
    }

    private class LoggingAdapter extends KeyValueStreamAdapter<DataField, String> {
        @Override
        public KeyValueStreamAdapter<DataField, String> begin() {
            logger.debug("begin");
            return this;
        }

        @Override
        public KeyValueStreamAdapter<DataField, String> keyValue(DataField key, String value) {
            if (logger.isDebugEnabled()) {
                for (Field f : key) {
                    logger.debug("tag={} ind={} subf={} data={}",
                            f.tag(), f.indicator(), f.subfieldId(), f.data());
                }
            }
            return this;
        }

        @Override
        public KeyValueStreamAdapter<DataField, String> end() {
            logger.debug("end");
            return this;
        }
    }

}
