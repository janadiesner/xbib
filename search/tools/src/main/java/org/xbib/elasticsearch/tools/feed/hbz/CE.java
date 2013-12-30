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
package org.xbib.elasticsearch.tools.feed.hbz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;

import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.tools.Feeder;
import org.xbib.io.InputService;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.Pipeline;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResourceContext;

public class CE extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(CE.class.getSimpleName());

    private final static SimpleResourceContext ctx = new SimpleResourceContext();

    public static void main(String[] args) {
        try {
            new CE()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private CE() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new CE();
            }
        };
    }

    @Override
    protected CE prepare(Ingest output) {
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String title = null;
            String author = null;
            String year = null;
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (title == null && line.startsWith("Titel:")) {
                    title = line.substring("Titel:".length()).trim();
                } else if (author == null && line.startsWith("Autor:")) {
                    author = line.substring("Autor:".length()).trim();
                } else if (year == null && line.startsWith("Jahr:")) {
                    year = line.substring("Jahr:".length()).trim();
                } else if (line.startsWith("ocr-text:")) {
                    sb.append(line.substring("ocr-text:".length()).trim()).append(" ");
                } else {
                    sb.append(line).append(" ");
                }
            }
            String id = uri.getPath();
            if (id.endsWith(".txt")) {
                int pos = id.lastIndexOf("/");
                id = pos >= 0 ? id.substring(pos + 1) : id;
                // remove .txt and force uppercase
                id = id.substring(0, id.length() - 4).toUpperCase();
                IRI identifier = IRI.builder().scheme("urn").host("hbz").query("enrichment").fragment(id).build();
                Resource resource = ctx.newResource();
                resource.id(identifier)
                        .add("dc:title", title)
                        .add("dc:creator", author)
                        .add("dc:date", year)
                        .newResource("dc:description")
                        .add("dcterms:tableOfContents", sb.toString());
                sink.output(ctx, ctx.contentBuilder());
            }
        }
    }
}
