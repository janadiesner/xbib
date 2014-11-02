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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.iri.namespace.CompactingNamespaceContext;
import org.xbib.rdf.context.ContentBuilder;
import org.xbib.rdf.context.ResourceContextWriter;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * A default content builder for building XContent from a resource and write it out.
 *
 * @param <C> context type
 * @param <R> resource type
 */
public class DefaultContentBuilder<C extends ResourceContext<R>, R extends Resource>
    implements ContentBuilder<C,R>, ResourceContextWriter {

    private XContentBuilder builder;

    public ResourceContextWriter builder(XContentBuilder builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public void write(ResourceContext resourceContext) throws IOException {
        if (builder != null) {
            builder.startObject();
            build(builder, (C) resourceContext, resourceContext.getResource());
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
        // first, the values
        for (IRI predicate : resource.predicates()) {
            Collection<Node> values = resource.objects(predicate);
            if (values == null) {
                throw new IllegalArgumentException("can't build property value set for predicate URI " + predicate);
            }
            // drop values with size 0 silently
            if (values.size() == 1) {
                // single value
                Node object = values.iterator().next();
                if (object instanceof Resource) {
                    Resource id = (Resource) object;
                    if (!id.isEmbedded()) {
                        builder.field(context.compact(predicate), id.id().toString());
                    }
                } else {
                    Object o = ((Literal)object).object();
                    if (o != null) {
                        builder.field(context.compact(predicate), o);
                    }
                }
                // drop null value
            } else if (values.size() > 1) {
                // array of values
                Collection<Node> properties = filterNodes(values);
                if (!properties.isEmpty()) {
                    builder.startArray(context.compact(predicate));
                    for (Node object : properties) {
                        if (object instanceof Resource) {
                            Resource id = (Resource) object;
                            if (!id.isEmbedded()) {
                                builder.value(id.id().toString());
                            }
                        } else {
                            // drop null values
                            Object o = ((Literal)object).object();
                            if (o != null) {
                                builder.value(o);
                            }
                        }
                    }
                    builder.endArray();
                }
            }
        }
        // then, iterate over resources
        for (IRI predicate : resource.predicates()) {
            List<Resource> resources = resource.resources(predicate);
            final List<XContentBuilder> list = new LinkedList<XContentBuilder>();
            resources.forEach(node -> {
                if (node.isEmbedded()) {
                    try {
                        XContentBuilder resBuilder = jsonBuilder();
                        resBuilder.startObject();
                        build(resBuilder, resourceContext, node);
                        resBuilder.endObject();
                        list.add(resBuilder);
                    } catch (IOException e) {
                        // ignore
                    }
                }
            });
            if (list.size() == 1) {
                builder.field(context.compact(predicate));
                builder.value(list.get(0));
            } else if (list.size() > 1) {
                builder.startArray(context.compact(predicate));
                builder.copy(list);
                builder.endArray();
            }
        }
    }

    private <O extends Node> Collection<O> filterNodes(Collection<O> objects) {
        Collection<O> nodes = new LinkedList<O>();
        for (O object : objects) {
            // drop embedded node ids
            if (object instanceof Resource) {
                Resource id = (Resource)object;
                if (id.isEmbedded()) {
                    continue;
                }
            }
            // drop null values
            if (object != null) {
                nodes.add(object);
            }
        }
        return nodes;
    }

}