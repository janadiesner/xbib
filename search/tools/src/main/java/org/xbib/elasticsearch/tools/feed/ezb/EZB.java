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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import javax.xml.namespace.QName;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.tools.Feeder;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.Pipeline;
import org.xbib.io.InputService;
import org.xbib.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xml.sax.SAXException;

/**
 * Elasticsearch indexer for "Elektronische Zeitschriftenbibliothek" (EZB)
 *
 * Format documentation
 *
 * http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf
 *
 */
public final class EZB extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(EZB.class.getSimpleName());

    private final static SimpleResourceContext resourceContext = new SimpleResourceContext();

    public static void main(String[] args) {
        try {
            new EZB()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private EZB() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new EZB();
            }
        };
    }

    @Override
    public EZB prepare(Ingest output) {
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        AbstractXmlHandler handler = new Handler(resourceContext)
                .setListener(new ResourceBuilder())
                .setDefaultNamespace("ezb", "http://ezb.uni-regensburg.de/ezeit/");
        InputStream in = InputService.getInputStream(uri);
        new XmlReader()
                .setNamespaces(false)
                .setHandler(handler)
                .parse(in);
        in.close();
    }

    private class Handler extends AbstractXmlResourceHandler {

        public Handler(ResourceContext ctx) {
            super(ctx);
        }

        @Override
        public void endElement (String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("license_entry_id".equals(name.getLocalPart()) && identifier == null) {
                IRI id = IRI.builder().scheme("iri")
                        .host(settings.get("index"))
                        .query(settings.get("type")).fragment(value).build();
                resourceContext.resource().id(id);
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "license_set".equals(name.getLocalPart());
        }

        @Override
        public void closeResource() {
            try { 
                sink.output(resourceContext, resourceContext.contentBuilder());
            } catch (IOException e ) {
                logger.error(e.getMessage(), e);
            }
            super.closeResource();
        }

        @Override
        public boolean skip(QName name) {
            return "ezb-export".equals(name.getLocalPart())
            || "release".equals(name.getLocalPart())
            || "version".equals(name.getLocalPart())
            || name.getLocalPart().startsWith("@");
        }
        
        @Override
        public Object toObject(QName name, String content) {
            switch (name.getLocalPart()) {
                case "reference_url":
                    // fall-through
                case "readme_url":
                    return URIUtil.decode(content, Charset.forName("UTF-8"));
                case "zdbid": {
                    return content.replaceAll("\\-", "").toLowerCase();
                }
                case "type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1: return "full-text-online"; //"Volltext nur online";
                        case 2: return "full-text-online-and-print"; //"Volltext online und Druckausgabe";
                        case 9: return "local"; //"lokale Zeitschrift";
                        case 11: return "digitized"; //"retrodigitalisiert";
                        default: throw new IllegalArgumentException("unknown type_id: " + content);
                    }
                }
                case "license_type_id" : {
                    switch (Integer.parseInt(content)) {
                        case 1 : return "local-license"; // "Einzellizenz";
                        case 2 : return "consortia-license"; //"Konsortiallizenz";
                        case 4 : return "supra-regional-license"; // "Nationallizenz";
                        default: throw new IllegalArgumentException("unknown license_type_id: " + content);
                    }
                }
                case "price_type_id" : {
                    switch (Integer.parseInt(content)) {
                        case 1 : return "no-fee"; //"lizenzfrei";
                        case 2 : return "no-fee-included-in-print"; //"Kostenlos mit Druckausgabe";
                        case 3 : return "fee"; //"Kostenpflichtig";
                        default: throw new IllegalArgumentException("unknown price_type_id: " + content);
                    }
                }
                case "ill_code" : {
                    switch (content) {
                        case "n" : return "no"; // "nein";
                        case "l" : return "copy-loan"; //"ja, Leihe und Kopie";
                        case "k" : return "copy"; //"ja, nur Kopie";
                        case "e" : return "copy-electronic";  //"ja, auch elektronischer Versand an Nutzer";
                        case "ln" : return "copy-loan-domestic";  //"ja, Leihe und Kopie (nur Inland)";
                        case "kn" : return "copy-domestic";  //"ja, nur Kopie (nur Inland)";
                        case "en" : return "copy-electronic-domestic";  //"ja, auch elektronischer Versand an Nutzer (nur Inland)";
                        default: throw new IllegalArgumentException("unknown ill_code: " + content);
                    }
                }
            }
            return super.toObject(name, content);
        }
    }

    private class ResourceBuilder implements TripleListener {

        @Override
        public TripleListener startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public TripleListener endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public ResourceBuilder newIdentifier(IRI identifier) {
            resourceContext.resource().id(identifier);
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            resourceContext.resource().add(triple);
            return this;
        }
    }

}