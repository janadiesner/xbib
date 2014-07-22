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
import org.xbib.rdf.ResourceFactory;
import org.xbib.rdf.io.TripleListener;

import java.util.Collection;

/**
 * A Resource context.
 * <p>
 * Resource contexts are useful when many resources are stored together
 * and common information about processing must be maintained,
 * for example, creation, building, and output.
 */
public interface ResourceContext<R extends Resource> extends ResourceFactory<R>, TripleListener {

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
     * @param writer the writer
     * @return the current resource context
     */
    ResourceContext<R> setContentBuilder(ResourceContextContentBuilder<ResourceContext<R>, R> writer);

    /**
     * Get content builder
     *
     * @return
     */
    ResourceContextContentBuilder<ResourceContext<R>, R> getContentBuilder();

    /**
     * Prepare the context for output.
     *
     * @return this context
     */
    ResourceContext<R> beforeOutput();

    ResourceContext<R> afterOutput();

}
