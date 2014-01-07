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
package org.xbib.elasticsearch.tools.feed.ezb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.tools.Feeder;
import org.xbib.io.InputService;
import org.xbib.io.NullWriter;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;

/**
 * Ingest inter library loan codes from EZB web service into Elasticsearch
 */
public class EZBWeb extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(EZBWeb.class.getName());

    private final static IRINamespaceContext context = IRINamespaceContext.newInstance();
    static {
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("xbib", "http://xbib.org/elements/1.0/");
    }

    private final static SimpleResourceContext resourceContext = new SimpleResourceContext();

    public static void main(String[] args) {
        try {
            new EZBWeb()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private EZBWeb() {
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
    public EZBWeb prepare(Ingest output) {
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        NullWriter nw = new NullWriter();
        resourceContext.newNamespaceContext(context);
        final TurtleWriter turtle = new TurtleWriter()
                .setContext(context)
                .output(nw);
        Iterator<String> it = readZDBIDs(new InputStreamReader(in, "UTF-8"));
        long counter = 0;
        while (it.hasNext()) {
            String zdbid = it.next();
            // blacklisting - EZB returns error?
            if ("20353625".equals(zdbid)
                    || "24756763".equals(zdbid)
                    || "24306423".equals(zdbid)
                    || "24306368".equals(zdbid)
                    || "24308262".equals(zdbid)
                    || "2611110x".equals(zdbid)
                    || "26431233".equals(zdbid)
                    || "26464901".equals(zdbid)
                    || "25510927".equals(zdbid)
                    || "26672285".equals(zdbid)
                    || "20280476".equals(zdbid)
                    || "21761747".equals(zdbid)
                    || "21977550".equals(zdbid)
                    || "24893158".equals(zdbid)
                    || "25528828".equals(zdbid)
                    || "14780884".equals(zdbid)
                    || "21776106".equals(zdbid)
                    || "20479992".equals(zdbid)
                    || "23236541".equals(zdbid)
                    || "26465401".equals(zdbid)
                    || "26669626".equals(zdbid)
                    || "20674405".equals(zdbid)
                    || "22059167".equals(zdbid)
                    || "26538556".equals(zdbid)
                    || "22538264".equals(zdbid)
                    || "24862058".equals(zdbid)
                    || "22059817".equals(zdbid)
                    || "20774540".equals(zdbid)
                    || "21283448".equals(zdbid)
                    || "21358904".equals(zdbid)

          ) {
                continue;
            }
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
                    String name = scanner.next();
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
                    // fixings
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
                    String key = zdbid + "_" + isil + "_" + firstDate + "_" + lastDate + "_" + (movingWall.isEmpty()? "0" : movingWall) ;
                    IRI id = IRI.builder()
                            .scheme("iri")
                            .host(settings.get("index"))
                            .query(settings.get("type"))
                            .fragment(key)
                            .build();
                    Resource resource = resourceContext.newResource();
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
                    turtle.write(resource);
                    sink.output(resourceContext, resourceContext.contentBuilder());
                    counter++;
                    if (counter % 1000 == 0) {
                        logger.info("{}", counter);
                    }
                } catch (NoSuchElementException e) {
                    logger.info("{}", zdbid);
                    logger.error(e.getMessage(), e);
                }
            }
            br.close();
        }
        turtle.close();
        writer.close();
        reader.close();
    }

    private Iterator<String> readZDBIDs(Reader reader) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> l = mapper.readValue(reader, List.class);
        return l.iterator();
    }

}
