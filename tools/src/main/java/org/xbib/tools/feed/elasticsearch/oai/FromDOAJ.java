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
package org.xbib.tools.feed.elasticsearch.oai;

import org.xbib.iri.IRI;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Context;
import org.xbib.rdf.memory.MemoryContext;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.XSDResourceIdentifiers;
import org.xbib.tools.OAIFeeder;

import javax.xml.namespace.QName;

/**
 * OAI harvester for DOAJ
 */
public class FromDOAJ extends OAIFeeder {

    @Override
    public String getName() {
        return "doaj-es";
    }

    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new FromDOAJ();
            }
        };
    }

    @Override
    protected RdfResourceHandler rdfResourceHandler() {
        return new DOAJResourceHandler(new MemoryContext());
    }

    class DOAJResourceHandler extends RdfResourceHandler {

        public DOAJResourceHandler(Context context) {
            super(context);
        }

        @Override
        public IRI toProperty(IRI property) {
            if ("issn".equals(property.getSchemeSpecificPart())) {
                return IRI.builder().curie("dc", "identifier").build();
            }
            if ("eissn".equals(property.getSchemeSpecificPart())) {
                return IRI.builder().curie("dc", "identifier").build();
            }
            return property;
        }

        @Override
        public Object toObject(QName name, String content) {
            switch (name.getLocalPart()) {
                case "identifier": {
                    if (content.startsWith("http://")) {
                        return new MemoryLiteral(content).type(XSDResourceIdentifiers.ANYURI);
                    }
                    if (content.startsWith("issn: ")) {
                        return new MemoryLiteral(content.substring(6)).type(ISSN);
                    }
                    if (content.startsWith("eissn: ")) {
                        return new MemoryLiteral(content.substring(7)).type(EISSN);
                    }
                    break;
                }
                case "subject": {
                    if (content.startsWith("LCC: ")) {
                        return new MemoryLiteral(content.substring(5)).type(LCCN);
                    }
                    break;
                }
                case "issn": {
                    return new MemoryLiteral(content.substring(0, 4) + "-" + content.substring(4)).type(ISSN);
                }
                case "eissn": {
                    return new MemoryLiteral(content.substring(0, 4) + "-" + content.substring(4)).type(EISSN);
                }
            }
            return super.toObject(name, content);
        }

        private final IRI ISSN = IRI.create("urn:ISSN");

        private final IRI EISSN = IRI.create("urn:EISSN");

        private final IRI LCCN = IRI.create("urn:LCC");

    }
}
