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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.content.RdfXContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.xml.XmlContentParser;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.tools.TimewindowFeeder;
import org.xbib.util.URIUtil;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer for "Elektronische Zeitschriftenbibliothek" (EZB)
 * <p>
 * Format documentation
 * <p>
 * http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf
 */
public final class EZBXML extends TimewindowFeeder {

    private final static Logger logger = LogManager.getLogger(EZBXML.class.getSimpleName());

    @Override
    public String getName() {
        return "ezb-xml-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return EZBXML::new;
    }


    protected String getIndex() {
        return settings.get("index", "ezbxml");
    }

    protected String getType() {
        return settings.get("type", "ezbxml");
    }

    @Override
    public void process(URI uri) throws Exception {
        IRINamespaceContext namespaceContext = IRINamespaceContext.getInstance();
        RdfContentParams params = new RdfXContentParams(namespaceContext);
        EZBHandler handler = new EZBHandler(params);
        handler.setDefaultNamespace("ezb", "http://ezb.uni-regensburg.de/ezeit/");
        InputStream in = InputService.getInputStream(uri);
        new XmlContentParser(in)
                .setNamespaces(false)
                .setHandler(handler)
                .parse();
        in.close();
    }

    @Override
    protected EZBXML cleanup() throws IOException {
        if (settings.getAsBoolean("aliases", false) && !settings.getAsBoolean("mock", false) && ingest.client() != null) {
            updateAliases();
        } else {
            logger.info("not doing alias settings");
        }
        ingest.stopBulk(getConcreteIndex());
        super.cleanup();
        return this;
    }

    class EZBHandler extends AbstractXmlResourceHandler {

        private String id;

        public EZBHandler(RdfContentParams params) {
            super(params);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("license_entry_id".equals(name.getLocalPart()) && identifier == null) {
                this.id = value;
                if (settings.get("identifier") != null) {
                    this.id = "(" + settings.get("identifier") + ")" + value;
                }
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "license_set".equals(name.getLocalPart());
        }

        @Override
        public void closeResource() throws IOException {
            super.closeResource();
            if (settings.get("collection") != null) {
                getResource().add("collection", settings.get("collection"));
            }
            RouteRdfXContentParams params = new RouteRdfXContentParams(getNamespaceContext(),
                    getConcreteIndex(), getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), id, content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{}", builder.string());
            }
            if (executor != null) {
                // tell executor we increased document count by one
                executor.metric().mark();
                if (executor.metric().count() % 10000 == 0) {
                    try {
                        writeMetrics(executor.metric(), null);
                    } catch (Exception e) {
                        throw new IOException("metric failed", e);
                    }
                }
            }
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
                            return "online"; //"Volltext nur online";
                        case 2:
                            return "online-and-print"; //"Volltext online und Druckausgabe";
                        case 9:
                            return "self-hosted"; //"lokale Zeitschrift";
                        case 11:
                            return "digitalization"; //"retrodigitalisiert";
                        default:
                            throw new IllegalArgumentException("unknown type_id: " + content);
                    }
                }
                case "license_type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "solitary"; // "Einzellizenz";
                        case 2:
                            return "consortial"; //"Konsortiallizenz";
                        case 4:
                            return "national"; // "Nationallizenz";
                        default:
                            throw new IllegalArgumentException("unknown license_type_id: " + content);
                    }
                }
                case "price_type_id": {
                    switch (Integer.parseInt(content)) {
                        case 1:
                            return "no"; //"lizenzfrei";
                        case 2:
                            return "no-with-print"; //"Kostenlos mit Druckausgabe";
                        case 3:
                            return "yes"; //"Kostenpflichtig";
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
