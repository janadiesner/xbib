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
package org.xbib.rdf.io.json;

import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Identifiable;
import org.xbib.rdf.Property;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.context.ResourceContextWriter;
import org.xbib.rdf.memory.MemoryResource;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Map;

public class JsonWriter<S extends Identifiable, P extends Property, O extends Node, C extends ResourceContext<Resource>>
        implements ResourceContextWriter<C, Resource>, Triple.Builder, Closeable, Flushable {

    private final static Logger logger = LoggerFactory.getLogger(JsonWriter.class.getName());

    private Resource resource;

    private boolean nsWritten;

    private StringBuilder sb;

    private IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    private C resourceContext;

    private String sortLangTag;

    public JsonWriter() {
        this.nsWritten = false;
        this.resource = new MemoryResource();
        this.sb = new StringBuilder();
    }


    @Override
    public void close() throws IOException {
        // write last resource
        write(resourceContext);
    }

    public JsonWriter<S, P, O, C> setNamespaceContext(IRINamespaceContext context) {
        this.namespaceContext = context;
        return this;
    }

    public JsonWriter<S, P, O, C> setSortLanguageTag(String languageTag) {
        this.sortLangTag = languageTag;
        return this;
    }

    @Override
    public JsonWriter newIdentifier(IRI iri) {
        if (!iri.equals(resource.id())) {
            try {
                if (!nsWritten) {
                    writeNamespaces();
                }
                // TODO
                resource = new MemoryResource();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        resource.id(iri);
        return this;
    }

    @Override
    public Triple.Builder begin() {
        return this;
    }

    @Override
    public Triple.Builder startPrefixMapping(String prefix, String uri) {
        return this;
    }

    @Override
    public Triple.Builder endPrefixMapping(String prefix) {
        return this;
    }

    @Override
    public Triple.Builder triple(Triple triple) {
        return this; // TODO
    }

    @Override
    public Triple.Builder end() {
        return this;
    }

    public JsonWriter writeNamespaces() throws IOException {
        if (namespaceContext == null) {
            return this;
        }
        nsWritten = false;
        for (Map.Entry<String, String> entry : namespaceContext.getNamespaces().entrySet()) {
            if (entry.getValue().length() > 0) {
                String nsURI = entry.getValue().toString();
                if (!RdfConstants.NS_URI.equals(nsURI)) {
                    writeNamespace(entry.getKey(), nsURI);
                    nsWritten = true;
                }
            }
        }
        return this;
    }

    private void writeNamespace(String prefix, String name) throws IOException {
        // TODO
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(C resourceContext) throws IOException {

    }
}
