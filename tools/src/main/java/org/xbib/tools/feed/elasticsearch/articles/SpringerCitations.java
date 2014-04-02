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
package org.xbib.tools.feed.elasticsearch.articles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.tools.Feeder;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.io.InputService;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.Pipeline;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResourceContext;

/**
 * Push Springer citations to Elasticsearch
 */
public class SpringerCitations extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(SpringerCitations.class.getSimpleName());

    private final static SimpleResourceContext resourceContext = new SimpleResourceContext();

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new SpringerCitations();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String title = null;
            List<String> author = new LinkedList();
            String year = null;
            String journal = null;
            String issn = null;
            String volume = null;
            String issue = null;
            String pagination = null;
            String doi = null;
            String publisher = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if ('%' != line.charAt(0)) {
                    continue;
                }
                char ch = line.charAt(1);
                switch (ch) {
                    case 'D' : {
                        year = line.substring(3).trim();
                        break;
                    }
                    case 'T' : {
                        title = line.substring(3).trim();
                        break;
                    }
                    case '@' : {
                        issn = line.substring(3).trim();
                        break;
                    }
                    case 'J' : {
                        journal = line.substring(3).trim();
                        break;
                    }
                    case 'A' : {
                        author.add(line.substring(3).trim());
                        break;
                    }
                    case 'V' : {
                        volume = line.substring(3).trim();
                        break;
                    }
                    case 'N' : {
                        issue = line.substring(3).trim();
                        break;
                    }
                    case 'P' : {
                        pagination = line.substring(3).trim();
                        break;
                    }
                    case 'R' : {
                        doi = line.substring(3).trim();
                        break;
                    }
                    case 'I' : {
                        publisher = line.substring(3).trim();
                        break;
                    }
                    case 'U' : {
                        // URL (DOI resolver)
                        break;
                    }
                    case 'K' : {
                        // keywords
                        break;
                    }
                    case '0' : {
                        // record type
                        break;
                    }
                    case '8' : {
                        // day
                        break;
                    }
                    case 'G' : {
                        // language
                        break;
                    }
                    default: {
                        logger.warn("unknown tag: " + line);
                    }
                }
            }
            String key = author.isEmpty() ? null : new WorkAuthor()
                    .authorName(author.get(0))
                    .workName(title)
                    .createIdentifier();
            IRI dereferencable = IRI.builder().scheme("http").host("xbib.info")
                    .path("/doi/").fragment(doi).build();
            Resource r = resourceContext.newResource()
                    .id(dereferencable)
                    .a(FABIO_ARTICLE)
                    .add("xbib:key", key)
                    .add("prism:doi", doi)
                    .add("dc:title", title);
            for (String a : author) {
                r.add("dc:creator", a);
            }
            r.add("prism:publicationDate", new SimpleLiteral(year).type(Literal.GYEAR));
            r.newResource(FRBR_EMBODIMENT)
                    .a(FABIO_PERIODICAL_VOLUME)
                    .add("prism:volume", volume);
            r.newResource(FRBR_EMBODIMENT)
                    .a(FABIO_PERIODICAL_ISSUE)
                    .add("prism:number", issue);
            r.newResource(FRBR_EMBODIMENT)
                    .a(FABIO_PRINT_OBJECT)
                    .add("prism:pageRange", pagination);
            r.newResource(FRBR_PARTOF)
                    .a(FABIO_JOURNAL)
                    .add("prism:publicationName", journal)
                    .add("prism:issn", issn)
                    .add("dc:publisher", publisher);
            resourceContext.getResource().id(IRI.builder()
                    .scheme("http")
                    .host(settings.get("index"))
                    .query(settings.get("type"))
                    .fragment(resourceContext.getResource().id().getFragment())
                    .build());
            sink.output(resourceContext, resourceContext.getResource(), resourceContext.getContentBuilder());
        }
    }

    private final IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private final IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    private final IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    private final IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    private final IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    private final IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    private final IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

}
