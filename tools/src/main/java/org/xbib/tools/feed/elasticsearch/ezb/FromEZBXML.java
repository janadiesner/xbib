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

import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.ContentBuilder;
import org.xbib.rdf.content.DefaultContentBuilder;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.Feeder;
import org.xbib.util.URIUtil;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Elasticsearch indexer for "Elektronische Zeitschriftenbibliothek" (EZB)
 * <p>
 * Format documentation
 * <p>
 * http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf
 */
public final class FromEZBXML extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(FromEZBXML.class.getSimpleName());

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromEZBXML();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        IRINamespaceContext namespaceContext = IRINamespaceContext.getInstance();
        ResourceContext<Resource> resourceContext = new SimpleResourceContext()
                .setNamespaceContext(namespaceContext)
                .setContentBuilder(contentBuilder(namespaceContext));

        AbstractXmlHandler handler = new EZBHandler(resourceContext)
                .setDefaultNamespace("ezb", "http://ezb.uni-regensburg.de/ezeit/");

        InputStream in = InputService.getInputStream(uri);
        new XmlReader().setNamespaces(false)
                    .setHandler(handler)
                    .parse(new InputStreamReader(in, "UTF-8"), null);
        in.close();
    }

    protected ContentBuilder contentBuilder(IRINamespaceContext namespaceContext) {
        return new DefaultContentBuilder<>();
    }

    class EZBHandler extends AbstractXmlResourceHandler {

        public EZBHandler(ResourceContext resourceContext) {
            super(resourceContext);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("license_entry_id".equals(name.getLocalPart()) && identifier == null) {
                IRI id = IRI.builder().scheme("iri")
                        .host(settings.get("index"))
                        .query(settings.get("type"))
                        .fragment(value)
                        .build();
                resourceContext().getResource().id(id);
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "license_set".equals(name.getLocalPart());
        }

        @Override
        public void closeResource() {
            // attach closeResource to output write
            try {
                if (resourceContext().getResource() != null) {
                    sink.output(resourceContext(), resourceContext().getResource(), resourceContext().getContentBuilder());
                } else {
                    logger.warn("no resource to output");
                }
            } catch (IOException e) {
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
                        case 1:
                            return "full-text-online"; //"Volltext nur online";
                        case 2:
                            return "full-text-online-and-print"; //"Volltext online und Druckausgabe";
                        case 9:
                            return "local"; //"lokale Zeitschrift";
                        case 11:
                            return "digitized"; //"retrodigitalisiert";
                        default:
                            throw new IllegalArgumentException("unknown type_id: " + content);
                    }
                }
                case "license_type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "local-license"; // "Einzellizenz";
                        case 2:
                            return "consortia-license"; //"Konsortiallizenz";
                        case 4:
                            return "supra-regional-license"; // "Nationallizenz";
                        default:
                            throw new IllegalArgumentException("unknown license_type_id: " + content);
                    }
                }
                case "price_type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "no-fee"; //"lizenzfrei";
                        case 2:
                            return "no-fee-included-in-print"; //"Kostenlos mit Druckausgabe";
                        case 3:
                            return "fee"; //"Kostenpflichtig";
                        default:
                            throw new IllegalArgumentException("unknown price_type_id: " + content);
                    }
                }
                case "ill_code": {
                    switch (content) {
                        case "n":
                            return "no"; // "nein";
                        case "l":
                            return "copy-loan"; //"ja, Leihe und Kopie";
                        case "k":
                            return "copy"; //"ja, nur Kopie";
                        case "e":
                            return "copy-electronic";  //"ja, auch elektronischer Versand an Nutzer";
                        case "ln":
                            return "copy-loan-domestic";  //"ja, Leihe und Kopie (nur Inland)";
                        case "kn":
                            return "copy-domestic";  //"ja, nur Kopie (nur Inland)";
                        case "en":
                            return "copy-electronic-domestic";  //"ja, auch elektronischer Versand an Nutzer (nur Inland)";
                        default:
                            throw new IllegalArgumentException("unknown ill_code: " + content);
                    }
                }
            }
            return super.toObject(name, content);
        }
    }

}
