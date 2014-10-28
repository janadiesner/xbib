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

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.context.ResourceContextWriter;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * Write RDF resources as Elasticsearch bulk format
 */
public class BulkFormatWriter implements ResourceContextWriter, Flushable, Closeable {

    private final Logger logger = LoggerFactory.getLogger(BulkFormatWriter.class.getName());

    private Writer writer;

    public BulkFormatWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void write(ResourceContext resourceContext) throws IOException {
        Resource resource = resourceContext.getResource();
        if (resource.id() == null) {
            return;
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
            StringBuilder sb = new StringBuilder();
            sb.append("{\"delete\":{\"_index\":\"")
                    .append(index).append("\",\"_type\":\"")
                    .append(type).append("\",\"_id\":\"")
                    .append(id).append("\"}}\n");
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
            StringBuilder sb = new StringBuilder();
            sb.append("{\"index\":{\"_index\":\"").append(index).append("\",\"_type\":\"").append(type).append("\",\"_id\":\"").append(id).append("\"}}\n")
                    .append(resourceContext.build(resource))
                    .append("\n");
            writer.write(sb.toString());
        }
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    /**
     * The IRI host is interpreted as the Elasticsearch index
     *
     * @param resource the resource
     * @return the index name
     */
    protected String makeIndex(Resource resource) {
        return resource.id().getHost();
    }

    /**
     * The IRI query is interpreted as the Elasticsearch index type
     *
     * @param resource the resource
     * @return the index type name
     */
    protected String makeType(Resource resource) {
        return resource.id().getQuery();
    }

    /**
     * The IRI fragment is  interpreted as the Elasticsearch document ID
     *
     * @param resource the resource
     * @return the doc id
     */
    protected String makeId(Resource resource) {
        String id = resource.id().getFragment();
        if (id == null) {
            id = resource.id().toString();
        }
        return id;
    }
}
