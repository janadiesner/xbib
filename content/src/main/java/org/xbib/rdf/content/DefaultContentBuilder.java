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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.iri.CompactingNamespaceContext;

import static com.google.common.collect.Lists.newLinkedList;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * A content builder for building XContent from a resource
 *
 * @param <C> context type
 * @param <R> resource type
 */
public class DefaultContentBuilder<C extends ResourceContext<R>, R extends Resource>
    implements ContentBuilder<C,R> {

    public DefaultContentBuilder<C,R> timestamp(Date timestamp) {
        return this;
    }

    public DefaultContentBuilder<C,R> message(String message) {
        return this;
    }

    public DefaultContentBuilder<C,R> source(String source) {
        return this;
    }

    public DefaultContentBuilder<C,R> sourceHost(String sourceHost) {
        return this;
    }

    public DefaultContentBuilder<C,R> sourcePath(String sorucePath) {
        return this;
    }

    public DefaultContentBuilder<C,R> type(String... type) {
        return this;
    }

    public DefaultContentBuilder<C,R> tags(String... tags) {
        return this;
    }

    public <S extends Identifier, P extends Property, O extends Node> String build(C context, R resource)
            throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        build(builder, context, resource);
        builder.endObject();
        return builder.string();
    }

    protected <S extends Identifier, P extends Property, O extends Node> void build(XContentBuilder builder, C resourceContext, Resource<S, P, O> resource)
            throws IOException {
        if (resource == null) {
            return;
        }
        CompactingNamespaceContext context = resourceContext.getNamespaceContext();
        // iterate over properties
        S subject = resource.subject();
        for (P predicate : resource.predicateSet(subject)) {
            Collection<O> values = resource.objects(predicate);
            if (values == null) {
                throw new IllegalArgumentException("can't build property value set for predicate URI " + predicate);
            }
            // drop values with size 0 silently
            if (values.size() == 1) {
                // single value
                O object = values.iterator().next();
                if (object instanceof Identifier) {
                    Identifier id = (Identifier) object;
                    if (!id.isBlank()) {
                        builder.field(context.compact(predicate.id()), id.id().toString()); // ID -> string
                    }
                } else if (object.nativeValue() != null) {
                    builder.field(context.compact(predicate.id()), object.nativeValue());
                }
                expandField(builder, resourceContext, subject, predicate, object);
                // drop null value
            } else if (values.size() > 1) {
                // array of values
                Collection<O> properties = filterBlankNodes(values);
                if (!properties.isEmpty()) {
                    builder.startArray(context.compact(predicate.id()));
                    for (O object : properties) {
                        if (object instanceof Identifier) {
                            Identifier id = (Identifier) object;
                            if (!id.isBlank()) {
                                builder.value(id.id().toString()); // IRI -> string
                            }
                        } else if (object.nativeValue() != null) {
                            // drop null values
                            builder.value(object.nativeValue());
                        }
                        expandValue(builder, resourceContext, subject, predicate, object);
                    }
                    builder.endArray();
                }
            }
        }
        // then, iterate over resources
        Map<P, Collection<Resource<S, P, O>>> m = resource.resources();
        for (P predicate : m.keySet()) {
            Collection<Resource<S, P, O>> resources = m.get(predicate);
            // drop resources with size 0 silently
            if (resources.size() == 1) {
                // single resource, check if resource is embedded
                Resource<S, P, O> res = resources.iterator().next();
                if (res.isBlank()) {
                    builder.startObject(context.compact(predicate.id()));
                    build(builder, resourceContext, res);
                    builder.endObject();
                }
            } else if (resources.size() > 1) {
                // build array of resources
                List<XContentBuilder> list = newLinkedList();
                for (Resource<S, P, O> child : resources) {
                    if (child.isBlank()) {
                        XContentBuilder resBuilder = jsonBuilder();
                        resBuilder.startObject();
                        build(resBuilder, resourceContext, child);
                        resBuilder.endObject();
                        list.add(resBuilder);
                    }
                }
                if (!list.isEmpty()) {
                    builder.startArray(context.compact(predicate.id()));
                    builder.copy(list);
                    builder.endArray();
                }
            }
        }
    }

    protected <S extends Identifier, P extends Property, O extends Node>
        void expandField(XContentBuilder builder, ResourceContext<R> context, S subject, P predicate, O object) throws IOException {
    }

    protected <S extends Identifier, P extends Property, O extends Node>
        void expandValue(XContentBuilder builder, ResourceContext<R> context, S subject, P predicate, O object) throws IOException {
    }

    private <O extends Node> Collection<O> filterBlankNodes(Collection<O> objects) {
        Collection<O> nodes = newLinkedList();
        for (O object : objects) {
            if (object instanceof Identifier) {
                Identifier id = (Identifier)object;
                if (id.isBlank()) {
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