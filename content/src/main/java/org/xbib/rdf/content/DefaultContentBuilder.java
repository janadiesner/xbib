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
package org.xbib.rdf.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.CompactingNamespaceContext;
import org.xbib.rdf.Context;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;
import org.xbib.rdf.ContentBuilder;
import org.xbib.rdf.ContextWriter;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * A default content builder for building XContent from a resource and write it out.
 *
 * @param <C> context type
 * @param <R> resource type
 */
public class DefaultContentBuilder<C extends Context<R>, R extends Resource>
    implements ContentBuilder<C,R>, ContextWriter {

    private XContentBuilder builder;

    public ContextWriter builder(XContentBuilder builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public void write(Context context) throws IOException {
        if (builder != null) {
            builder.startObject();
            build(builder, (C) context, context.getResource());
            builder.endObject();
        }
    }

    public String build(C context, R resource)  throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        build(builder, context, resource);
        builder.endObject();
        return builder.string();
    }

    protected void build(XContentBuilder builder, C resourceContext, Resource resource)
            throws IOException {
        if (resource == null) {
            return;
        }
        CompactingNamespaceContext context = resourceContext.getNamespaceContext();
        for (IRI predicate : resource.predicates()) {
            // first, the values
            final List<Object> values = new ArrayList<Object>(32);
            final List<Node> nodes = resource.visibleObjects(predicate);
            for (Node node : nodes) {
                if (node instanceof Resource) {
                    values.add(((Resource) node).id().toString()); // URLs
                } else if (node instanceof Literal) {
                    Object o = ((Literal)node).object();
                    if (o != null) {
                        values.add(o);
                    }
                }
            }
            if (values.size() == 1) {
                builder.field(context.compact(predicate), values.get(0));
            } else if (values.size() > 1) {
                builder.array(context.compact(predicate), values);
            }
            // second, the embedded resources
            final List<Resource> resources = resource.embeddedResources(predicate);
            if (resources.size() == 1) {
                builder.field(context.compact(predicate));
                builder.startObject();
                build(builder, resourceContext, resources.get(0));
                builder.endObject();
            } else if (resources.size() > 1) {
                builder.field(context.compact(predicate));
                builder.startArray();
                for (Resource res : resources) {
                    builder.startObject();
                    build(builder, resourceContext, res);
                    builder.endObject();
                }
                builder.endArray();
            }
        }
    }

}