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
package org.xbib.rdf.context;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

import java.io.IOException;
import java.util.Collection;


public interface ResourceContext<R extends Resource> extends Triple.Builder {

    R newResource();

    /**
     * Get collected resource
     *
     * @return a collection of resources
     */
    Collection<R> getResources();

    /**
     * Set a new IRI namespace context
     *
     * @param namespaceContext namespaces
     * @return this context
     */
    ResourceContext<R> setNamespaceContext(IRINamespaceContext namespaceContext);

    /**
     * Get IRI namespace context
     *
     * @return namespace context
     */
    IRINamespaceContext getNamespaceContext();

    /**
     * Switch to this resource in this context.
     *
     * @param resource the resource
     * @return the current resource context
     */
    ResourceContext<R> switchTo(R resource);

    /**
     * Get resource in this context.
     *
     * @return current resource
     */
    R getResource();

    /**
     * Set content builder
     *
     * @param builder the content builder
     * @return the current resource context
     */
    ResourceContext<R> setContentBuilder(ContentBuilder<ResourceContext<R>, R> builder);

    ResourceContext<R> setWriter(ResourceContextWriter writer);

    /**
     * Prepare the context for output.
     *
     * @return this context
     */
    ResourceContext<R> beforeBuild();

    ResourceContext<R> afterBuild();

    String build(R resource) throws IOException;

    void write() throws IOException;

}
