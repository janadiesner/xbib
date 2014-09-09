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
package org.xbib.tools.convert.zdb.bibdat;

import org.xbib.elements.marc.dialects.pica.PicaContext;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilder;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilderFactory;
import org.xbib.elements.marc.dialects.pica.PicaElementMapper;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.DataField;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.dialects.pica.DNBPICAXmlReader;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.AbstractResourceContextWriter;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.tools.Converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.Normalizer;

public final class BibdatZDB extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(BibdatZDB.class.getName());

    @Override
    public String getName() {
        return "zdb-bibdat";
    }

    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new BibdatZDB();
            }
        };
    }

    protected BibdatZDB prepare() throws IOException {
        super.prepare();
        out.init(settings.get("output", "bibdat.nt"));
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bibdat")
                .pipelines(settings.getAsInt("pipelines", 1))
                .detectUnknownKeys(settings.getAsBoolean("detect", false))
                .start(new PicaElementBuilderFactory() {
                    public PicaElementBuilder newBuilder() {
                        PicaElementBuilder builder = new PicaElementBuilder();
                        builder.addWriter(out);
                        return builder;
                    }
                });
        logger.info("mapper is up, {} elemnents", mapper.map().size());
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(new StringTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<DataField, String>() {
                    @Override
                    public KeyValueStreamAdapter<DataField, String> keyValue(DataField key, String value) {
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
        InputStream in = InputService.getInputStream(uri);
        new DNBPICAXmlReader().setListener(kv).parse(in);
        in.close();
        mapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("detected unknown elements = {}", mapper.getUnknownKeys());
        }
    }

    private final static OurContextResourceOutput out = new OurContextResourceOutput();

    private final static class OurContextResourceOutput extends AbstractResourceContextWriter<PicaContext, Resource> {

        File f;
        FileWriter fw;
        NTripleWriter writer;

        public OurContextResourceOutput init(String filename) throws IOException {
            this.f = new File(filename);
            this.fw = new FileWriter(f);
            this.writer = new NTripleWriter(fw);
                    //.setNullPredicate(IRI.builder().scheme("http").host("xbib.org").path("/adr").build());
            return this;
        }

        @Override
        public void write(PicaContext context) throws IOException {
            IRI id = IRI.builder().scheme("http").host("xbib.org").path("/pica/zdb/bibdat")
                    .fragment(context.getID()).build();
            context.getResource().id(id);
            writer.write(context);
        }

        public void close() throws IOException {
            if (fw != null) {
                fw.close();
            }
        }
    }

}
