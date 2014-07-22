/**
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
package org.xbib.rdf.context;

import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.simple.SimpleResource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An abstract resource context.
 *
 * @param <R> the resource type
 */
public abstract class AbstractResourceContext<R extends Resource> implements ResourceContext<R> {

    private IRINamespaceContext namespaceContext;

    private R resource;

    private Map<Object, R> resourceMap;


    public ResourceContext<R> newNamespaceContext() {
        this.namespaceContext = IRINamespaceContext.newInstance();
        return this;
    }

    public ResourceContext<R> setNamespaceContext(IRINamespaceContext namespaces) {
        this.namespaceContext = namespaces;
        return this;
    }

    public IRINamespaceContext getNamespaceContext() {
        if (namespaceContext == null) {
            this.namespaceContext = IRINamespaceContext.newInstance();
        }
        return namespaceContext;
    }

    @Override
    public ResourceContext<R> switchTo(R resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public R getResource() {
        return resource;
    }

    public Collection<R> getResources() {
        return resourceMap != null ? resourceMap.values() : null;
    }

    @Override
    public TripleListener begin() {
        return this;
    }

    @Override
    public TripleListener startPrefixMapping(String prefix, String uri) {
        getNamespaceContext().addNamespace(prefix, uri);
        return this;
    }

    @Override
    public TripleListener endPrefixMapping(String prefix) {
        // ignore
        return this;
    }

    @Override
    public TripleListener newIdentifier(IRI identifier) {
        // ignore
        return this;
    }

    @Override
    public TripleListener triple(Triple triple) {
        if (resourceMap == null) {
            resourceMap = new LinkedHashMap();
        }
        IRI iri = triple.subject().id();
        if (!resourceMap.containsKey(iri)) {
            resourceMap.put(iri, (R) new SimpleResource().id(iri));
        }
        resourceMap.get(iri).add(triple);
        return this;
    }

    @Override
    public TripleListener end() {
        return this;
    }

    public String toString() {
        return resource.toString() + "\n";
    }
}
