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
package org.xbib.tools.convert.articles;

import org.xbib.csv.CSVParser;
import org.xbib.grouping.bibliographic.endeavor.PublishedJournal;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.Converter;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Import serials list
 */
public class SerialsDB extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(SerialsDB.class.getSimpleName());

    private final static IRINamespaceContext context = IRINamespaceContext.newInstance();

    static {
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("prism", "http://prismstandard.org/namespaces/basic/2.1/");
    }

    private final static SimpleResourceContext resourceContext = new SimpleResourceContext();

    private final static Map<String, Resource> serials = new ConcurrentHashMap<>();

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new SerialsDB();
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {
        logger.info("next URI is {}", uri);
        InputStream in = InputService.getInputStream(uri);
        if (in == null) {
            throw new FileNotFoundException("not found: " + uri);
        }
        if (settings.get("output") == null) {
            throw new IllegalArgumentException("no output");
        }

        String fileName = settings.get("output") + ".ttl";
        FileWriter w = new FileWriter(fileName);

        resourceContext.setNamespaceContext(context);
        final TurtleWriter writer = new TurtleWriter(w);
        writer.setNamespaceContext(context);
        CSVParser parser = new CSVParser(new InputStreamReader(in, "UTF-8"));
        try {
            int i = 0;
            while (true) {
                String journalTitle = parser.nextToken().trim();
                logger.debug("{}", journalTitle);
                String publisher = parser.nextToken().trim();
                String subjects = parser.nextToken();
                String issn = parser.nextToken().trim();
                String doi = parser.nextToken().trim();
                String publicationStructure = parser.nextToken();
                String[] issnArr = issn.split("\\|");
                // skip fake titles
                if ("xxxx".equals(journalTitle)) {
                    continue;
                }
                if (i++ == 0) {
                    // head line
                    continue;
                }
                Resource resource = resourceContext.newResource();
                String issn1 = buildISSN(issnArr, 0, true);
                String issn2 = buildISSN(issnArr, 1, true);
                if (issn1 != null && issn1.equals(issn2)) {
                    issn2 = null;
                }
                String key = new PublishedJournal()
                        .journalName(journalTitle)
                        .publisherName(publisher)
                        .createIdentifier();
                // journals are not "works", maybe "serials (not time-lined)", so they are "endeavors"
                IRI id = IRI.builder().scheme("http")
                        .host("xbib.info")
                        .path("/endeavors/" + key + "/")
                        .build();
                resource.id(id)
                        .add("dc:identifier", key)
                        .add("dc:publisher", publisher.isEmpty() ? null : publisher)
                        .add("dc:title", journalTitle)
                        .add("prism:issn", issn1)
                        .add("prism:issn", issn2)
                        .add("prism:doi", doi.isEmpty() ? null : doi);
                if (!serials.containsKey(journalTitle)) {
                    writer.write(resourceContext);
                    serials.put(journalTitle, resource);
                } else {
                    logger.info("ignoring double serial title: {}", journalTitle);
                }
            }
        } catch (EOFException e) {
            // ignore
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        FileWriter txt = new FileWriter(settings.get("output") + ".txt");
        for (String j : serials.keySet()) {
            txt.write(j + "|" + serials.get(j));
            txt.write("\n");
        }
        txt.close();

        logger.info("{} {}", this, getMap().size());
    }

    public Map<String, Resource> getMap() {
        return serials;
    }

    private String buildISSN(String[] issnArr, int i, boolean hyphen) {
        return issnArr.length > i && !issnArr[i].isEmpty() ?
                (hyphen ? new StringBuilder(issnArr[i].toLowerCase()).insert(4, '-').toString() : issnArr[i].toLowerCase()) :
                null;
    }

}
