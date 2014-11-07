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
package org.xbib.tools.feed.elasticsearch.ezb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.io.InputService;
import org.xbib.io.NullWriter;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Context;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.memory.MemoryContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.tools.Feeder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Ingest inter library loan codes from EZB web service into Elasticsearch
 */
public class EZBWeb extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(EZBWeb.class.getName());

    @Override
    public String getName() {
        return "ezb-web-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new EZBWeb();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        IRINamespaceContext namespaceContext = IRINamespaceContext.getInstance();
        namespaceContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        namespaceContext.addNamespace("xbib", "http://xbib.org/elements/1.0/");
        Context<Resource> context = new MemoryContext()
                //.setContentBuilder(contentBuilder(namespaceContext))
                .setNamespaceContext(namespaceContext);

        InputStream in = InputService.getInputStream(uri);
        NullWriter nw = new NullWriter();
        context.setNamespaceContext(namespaceContext);
        final TurtleWriter turtle = new TurtleWriter(nw);
        turtle.setNamespaceContext(namespaceContext);
        Iterator<String> it = readZDBIDs(new InputStreamReader(in, "UTF-8"));
        long counter = 0;
        while (it.hasNext()) {
            String zdbid = it.next();
            StringBuilder sb = new StringBuilder();
            sb.append(zdbid).insert(sb.length() - 1, '-');
            URL url = new URL(settings.get("baseURL") + sb.toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            br.readLine(); // ZDB-Id: ...
            br.readLine(); // Treffer: ...
            br.readLine(); // empty line
            Scanner scanner = new Scanner(br);
            scanner.useDelimiter("\t|\n");
            while (scanner.hasNext()) {
                try {
                    String sigel = scanner.next();
                    String isil = scanner.next();
                    String name = scanner.next(); // unused
                    String code1 = scanner.next();
                    String code2 = scanner.next();
                    String code3 = scanner.next();
                    String comment = scanner.next();
                    String firstDate = scanner.next();
                    String firstVolume = scanner.next();
                    String firstIssue = scanner.next();
                    String lastDate = scanner.next();
                    String lastVolume = scanner.next();
                    String lastIssue = scanner.next();
                    String movingWall = scanner.next();
                    // skip fake entry
                    if ("AAAAA".equals(sigel)) {
                        continue;
                    }
                    // fixes
                    if ("0".equals(firstVolume)) {
                        firstVolume = null;
                    }
                    if ("0".equals(firstIssue)) {
                        firstIssue = null;
                    }
                    if ("0".equals(lastVolume)) {
                        lastVolume = null;
                    }
                    if ("0".equals(lastIssue)) {
                        lastIssue = null;
                    }
                    // firstdate, lastdate might be empty -> ok
                    String key = zdbid + "."
                            + isil + "."
                            + firstDate + "."
                            + lastDate + "."
                            + (movingWall.isEmpty() ? "0" : movingWall);
                    IRI id = IRI.builder()
                            .scheme("iri")
                            .host(settings.get("index"))
                            .query(settings.get("type"))
                            .fragment(key)
                            .build();
                    Resource resource = context.newResource();
                    resource.id(id)
                            .add("dc:identifier", key)
                            .add("xbib:identifier", zdbid)
                            .add("xbib:isil", isil)
                            .add("xbib:firstDate", firstDate)
                            .add("xbib:firstVolume", firstVolume)
                            .add("xbib:firstIssue", firstIssue)
                            .add("xbib:lastDate", lastDate)
                            .add("xbib:lastVolume", lastVolume)
                            .add("xbib:lastIssue", lastIssue)
                            .add("xbib:interlibraryloanCode",
                                    (code1.isEmpty() ? "x" : code1)
                                            + (code2.isEmpty() ? "x" : code2)
                                            + (code3.isEmpty() ? "x" : code3))
                            .add("xbib:comment", comment);
                    // turtle
                    turtle.write(context);
                    // Elasticsearch
                    sink.write(context);
                    counter++;
                    if (counter % 1000 == 0) {
                        logger.info("{}", counter);
                    }
                } catch (NoSuchElementException e) {
                    logger.error("ZDBID=" + zdbid + " " + e.getMessage(), e);
                }
            }
            br.close();
        }
        if (turtle != null) {
            turtle.close();
        }
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
    }

    private Iterator<String> readZDBIDs(Reader reader) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> l = mapper.readValue(reader, List.class);
        return l.iterator();
    }

}
