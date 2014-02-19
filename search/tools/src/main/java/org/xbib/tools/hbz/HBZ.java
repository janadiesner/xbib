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
package org.xbib.tools.hbz;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;

import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.Pipeline;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.dialects.MarcXmlTarReader;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.tools.Converter;

/**
 * Converter tool for Hochschulbibliothekszentrum (HBZ) MAB data in MarcXml TAR clobs
 */
public final class HBZ extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(HBZ.class.getSimpleName());

    public static void main(String[] args) {
        try {
            new HBZ()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private HBZ() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new HBZ();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        final MABElementMapper mapper = new MABElementMapper(settings.get("elements"))
                .pipelines(settings.getAsInt("pipelines", 1))
                .detectUnknownKeys(settings.getAsBoolean("detect", true))
                .start(new MABElementBuilderFactory() {
                    public MABElementBuilder newBuilder() {
                        return new MABElementBuilder()
                                .addOutput(out);
                    }
                });
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                    @Override
                    public String transform(String value) {
                        return value;
                    }
                })
                .addListener(mapper);
        final MarcXmlTarReader reader = new MarcXmlTarReader()
                .setURI(uri)
                .setListener(kv);
        while (reader.hasNext()) {
            reader.next();
        }
        reader.close();
        if (settings.getAsBoolean("detect", true)) {
            logger.info("{}", mapper.unknownKeys());
        }
        mapper.close();
    }

    private final static OurElementOutput out = new OurElementOutput();

    private final static class OurElementOutput extends CountableElementOutput<ResourceContext,Resource> {

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            StringWriter sw = new StringWriter();
            new TurtleWriter().output(sw).write(context.resource());
            counter.incrementAndGet();
        }
    }

}
