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

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.tools.QueueFeeder;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.InputService;
import org.xbib.io.field.BufferedFieldStreamReader;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.element.PipelineElement;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.ContentBuilder;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.Normalizer;

/**
 * Indexing Zeitschriftendatenbank (ZDB) MARC ISO2709 files
 */
public final class QueueZDBFromMARC
        extends QueueFeeder<Boolean, PipelineRequest, Pipeline<Boolean, PipelineRequest>, QueueZDBFromMARC.MyElement> {

    private final static Logger logger = LoggerFactory.getLogger(QueueZDBFromMARC.class.getName());

    private final Charset UTF8 = Charset.forName("UTF-8");

    private final Charset ISO88591 = Charset.forName("ISO-8859-1");

    public static void main(String[] args) {
        try {
            new QueueZDBFromMARC()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private QueueZDBFromMARC() {
    }

    @Override
    protected QueueZDBFromMARC prepare(Ingest output) {
        // nothing special to do here...
        return this;
    }

    @Override
    protected MyElement getPoisonElement() {
        return new MyElement();
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStreamReader r = new InputStreamReader(InputService.getInputStream(uri), ISO88591);
        try (BufferedFieldStreamReader reader = new BufferedFieldStreamReader(r)) {
            queue().put(new MyElement().set(reader.readData()));
        }
    }

    @Override
    protected void process(MyElement element) throws IOException {
        final MARCElementMapper mapper = new MARCElementMapper(settings.get("elements"))
                .pipelines(settings.getAsInt("pipelines",1))
                .detectUnknownKeys(settings.getAsBoolean("detect", false))
                .start(new MARCElementBuilderFactory() {
                    public MARCElementBuilder newBuilder() {
                        return new MARCElementBuilder().addOutput(out);
                    }
                });
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(new String(value.getBytes(ISO88591), UTF8),
                                Normalizer.Form.NFKC);
                    }
                })
                .addListener(mapper);
        final Iso2709Reader isoReader = new Iso2709Reader()
                .setMarcXchangeListener(kv);
        try {
            isoReader.setProperty(Iso2709Reader.FORMAT, "MARC");
            if ("marc/holdings".equals(settings.get("elements")) || "marc/zdb/hol".equals(settings.get("elements"))) {
                isoReader.setProperty(Iso2709Reader.TYPE, "Holdings");
            }
            isoReader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
            isoReader.setProperty(Iso2709Reader.SILENT_ERRORS, true);
            isoReader.parse(new InputSource(new StringReader(element.get())));
        } catch (SAXException e) {
            throw new IOException(e);
        }

        mapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown keys={}", mapper.unknownKeys());
        }

    }

    public class MyElement implements PipelineElement<String> {

        private String s;

        @Override
        public String get() {
            return s;
        }

        @Override
        public MyElement set(String s) {
            this.s = s;
            return this;
        }
    }




    private final static OurElementOutput out = new OurElementOutput();

    private final static class OurElementOutput extends CountableElementOutput<ResourceContext, Resource> {

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            IRI id = IRI.builder().scheme("http").host(settings.get("index")).query(settings.get("type"))
                        .fragment(Long.toString(counter.incrementAndGet())).build();
            context.resource().id(id);
            sink.output(context, contentBuilder);
        }
    }

}
