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
package org.xbib.elasticsearch.rdf;

import java.io.IOException;

import org.xbib.elasticsearch.support.client.Feeder;
import org.xbib.elements.context.CountableContextResourceOutput;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.content.ContentBuilder;

/**
 * Index RDF resources into Elasticsearch
 *
 * @param <C>
 * @param <R>
 */
public class ResourceSink<C extends ResourceContext, R extends Resource>
        extends CountableContextResourceOutput<C, R> {

    private final Feeder feeder;

    public ResourceSink(final Feeder feeder) {
        this.feeder = feeder;
    }

    @Override
    public void output(C context, R resource, ContentBuilder<C, R> contentBuilder) throws IOException {
        if (resource == null) {
            throw new IOException("resource is null");
        }
        if (resource.id() == null) {
            throw new IOException("resource ID is null");
        }
        if (resource.isEmpty()) {
            return;
        }
        if (resource.isDeleted()) {
            String index = makeIndex(resource);
            if (index == null) {
                throw new IOException("index must not be null, no host set in IRI?");
            }
            String type = makeType(resource);
            if (type == null) {
                throw new IOException("type must not be null, no query set in IRI?");
            }
            String id = makeId(resource);
            if (id == null) {
                throw new IOException("id must not be null, no fragment set in IRI?");
            }
            feeder.delete(index, type, id);
        } else {
            String index = makeIndex(resource);
            if (index == null) {
                throw new IOException("index must not be null, no host set in IRI?");
            }
            String type = makeType(resource);
            if (type == null) {
                throw new IOException("type must not be null, no query set in IRI?");
            }
            String id = makeId(resource);
            if (id == null) {
                throw new IOException("id must not be null, no fragment set in IRI?");
            }
            feeder.index(index, type, id, contentBuilder.build(context, resource));

        }
        counter.incrementAndGet();
    }

    /**
     * The IRI host is interpreted as the Elasticsearch index
     *
     * @param resource the resource
     * @return the index
     */
    protected String makeIndex(R resource) {
        return resource.id().getHost();
    }

    /**
     * The IRI query is interpreted as the Elasticsearch index type
     *
     * @param resource the resource
     * @return the type
     */
    protected String makeType(R resource) {
        return resource.id().getQuery();
    }

    /**
     * The IRI fragment is interpreted as the Elasticsearch document ID
     *
     * @param resource the resource
     * @return the id
     */
    protected String makeId(R resource) {
        String id = resource.id().getFragment();
        if (id == null) {
            id = resource.id().toString();
        }
        return id;
    }
}
