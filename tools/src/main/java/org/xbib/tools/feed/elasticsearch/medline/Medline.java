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
package org.xbib.tools.feed.elasticsearch.medline;

import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.content.RdfXContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.xml.XmlContentParser;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.tools.Feeder;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for Medline XML files
 */
public final class Medline extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(Medline.class.getSimpleName());

    @Override
    public String getName() {
        return "medline-xml-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return Medline::new;
    }

    @Override
    public void process(URI uri) throws Exception {
        IRINamespaceContext namespaceContext = IRINamespaceContext.getInstance();
        RdfContentParams params = new RdfXContentParams(namespaceContext, false);
        AbstractXmlHandler handler = new Handler(params)
                .setDefaultNamespace("ml", "http://www.nlm.nih.gov/medline");
        InputStream in = InputService.getInputStream(uri);
        new XmlContentParser().setNamespaces(false)
                .setHandler(handler)
                .parse(new InputStreamReader(in, "UTF-8"));
        in.close();
    }

    private class Handler extends AbstractXmlResourceHandler {

        String id;

        public Handler(RdfContentParams params) {
            super(params);
        }

        @Override
        public void closeResource() throws IOException {
            super.closeResource();
            RouteRdfXContentParams params = new RouteRdfXContentParams(getNamespaceContext(),
                    settings.get("index", "ezbxml"),
                    settings.get("type", "ezbxml"));
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), id, content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.resource(getResource());
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "MedlineCitation".equals(name.getLocalPart());
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            // important: there are many occurances of PMID.
            // We must only take the first occurance for the ID.
            if (id == null && "PMID".equals(name.getLocalPart())) {
                this.id = value;
            }
        }

        @Override
        public boolean skip(QName name) {
            return "MedlineCitationSet".equals(name.getLocalPart())
                    || "MedlineCitation".equals(name.getLocalPart())
                    || "@Label".equals(name.getLocalPart())
                    || "@NlmCategory".equals(name.getLocalPart());
        }

        @Override
        public XmlHandler setNamespaceContext(IRINamespaceContext namespaceContext) {
            return this;
        }

        @Override
        public IRINamespaceContext getNamespaceContext() {
            return IRINamespaceContext.getInstance();
        }
    }

}
