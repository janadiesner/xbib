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
package org.xbib.rdf.memory;

import org.xbib.iri.IRI;
import org.xbib.rdf.Identifiable;
import org.xbib.rdf.Property;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.types.XSDIdentifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A resource is a sequence of properties and of associated resources.
 */
public class MemoryResource implements Comparable<Identifiable>, Resource, XSDIdentifiers {

    private final static AtomicLong nodeID = new AtomicLong();

    private final static String GENID = "genid";

    private final static String PLACEHOLDER = "_:";

    private final static IRI DELETED = IRI.builder().curie("_deleted").build();

    private final MultiMap<IRI, Node> attributes = new LinkedHashMultiMap<IRI, Node>();

    private final Map<IRI, Resource> children = new LinkedHashMap<IRI, Resource>();

    private IRI id;

    @Override
    public MemoryResource id(IRI id) {
        this.id = id;
        return this;
    }

    @Override
    public IRI id() {
        return id;
    }

    @Override
    public int compareTo(Identifiable o) {
        return id == null ? -1 : id.toString().compareTo(o.id().toString());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Identifiable && ((Identifiable) obj).id().equals(id);
    }

    public MemoryResource blank() {
        id(IRI.builder().curie(GENID, "b" + next()).build());
        return this;
    }

    public MemoryResource blank(String id) {
        id(IRI.builder().curie(GENID, id).build());
        return this;
    }

    public boolean isEmbedded() {
        return GENID.equals(id().getScheme());
    }

    public static void reset() {
        nodeID.set(0L);
    }

    public static long next() {
        return nodeID.incrementAndGet();
    }

    @Override
    public String toString() {
        if (id() == null) {
            blank();
        }
        return isEmbedded() ? PLACEHOLDER + id().getSchemeSpecificPart() : id().toString();
    }

    @Override
    public Resource add(Triple triple) {
        if (triple == null) {
            return this;
        }
        IRI id = triple.subject().id();
        if (id == null || id.equals(id())) {
            add(triple.predicate(), triple.object());
        } else {
            Resource r = children.get(id);
            if (r != null) {
                return r.add(triple);
            } else {
                // nothing found, continue with a new resource with new subject
                return new MemoryResource().id(id).add(triple);
            }
        }
        return this;
    }

    @Override
    public Resource add(Property predicate, Node object) {
        attributes.put(predicate.id(), object);
        if (object instanceof Resource) {
            Resource r = (Resource)object;
            children.put(r.id(), r);
        }
        return this;
    }

    @Override
    public Resource add(Property predicate, IRI iri) {
        return add(predicate, new MemoryResource().id(iri));
    }

    @Override
    public Resource add(Property predicate, Literal literal) {
        if (predicate != null && literal != null) {
            attributes.put(predicate.id(), literal);
        }
        return this;
    }

    @Override
    public Resource add(Property predicate, Resource resource) {
        if (resource == null) {
            return this;
        }
        if (resource.id() == null) {
            resource.id(id());
            Resource r = newResource(predicate);
            resource.triples().forEach(r::add);
        } else {
            attributes.put(predicate.id(), resource);
        }
        return this;
    }

    public Resource remove(Property predicate) {
        if (predicate == null || predicate.id() == null) {
            return this;
        }
        // check if child resource exists for any of the objects under this predicate and remove it
        embeddedResources(predicate.id()).forEach(resource -> children.remove(resource.id()));
        attributes.remove(predicate.id());
        return this;
    }

    public Resource remove(Property predicate, Node object) {
        if (predicate == null || predicate.id() == null) {
            return this;
        }
        return this;
    }


    @Override
    public Resource a(IRI externalResource) {
        add("rdf:type", externalResource);
        return this;
    }

    @Override
    public Set<IRI> predicates() {
        return attributes.keySet();
    }

    @Override
    public Collection<Node> objects(IRI predicate) {
        return attributes.get(predicate);
    }

    @Override
    public List<Literal> literals(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(n -> n instanceof Literal)
                .map(Literal.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> resources(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(n -> n instanceof Resource)
                .map(Resource.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> embeddedResources(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(n -> n instanceof Resource)
                .map(Resource.class::cast)
                .filter(Resource::isEmbedded)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> linkedResources(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(n -> n instanceof Resource)
                .map(Resource.class::cast)
                .filter(n -> !n.isEmbedded())
                .collect(Collectors.toList());
    }

    /**
     * Compact a predicate. Under the predicate, there is a single blank node
     * object with a single value for the same predicate. In such case, the
     * blank node can be removed and the single value can be promoted to the
     * predicate.
     *
     * @param predicate the predicate
     */
    @Override
    public void compactPredicate(IRI predicate) {
        List<Resource> resources = resources(predicate);
        Iterator<Resource> it = resources.iterator();
        Resource resource = it.next();
        if (!it.hasNext()) {
            List<Node> newResource = new ArrayList<Node>();
            for (Node node : objects(predicate)) {
                if (node instanceof Literal) {
                    newResource.add(node);
                } else if (node instanceof Resource) {
                    if (!((Resource) node).isEmbedded()) {
                        newResource.add(node);
                    }
                }
            }
            newResource.addAll(new ArrayList(resource.objects(predicate)));
            attributes.remove(predicate);
            attributes.putAll(predicate, newResource);
        }
    }

    @Override
    public void clear() {
        attributes.clear();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public Resource setDeleted(boolean delete) {
        attributes.remove(DELETED);
        attributes.put(DELETED, new MemoryLiteral(delete));
        return this;
    }

    @Override
    public boolean isDeleted() {
        Literal literal = (Literal) attributes.get(DELETED);
        return (literal.object() instanceof Boolean && (Boolean) literal.object());
    }

    @Override
    public Resource newResource(Property predicate) {
        Resource r = new MemoryResource().blank();
        children.put(r.id(), r);
        attributes.put(predicate.id(), r);
        return r;
    }

    @Override
    public Resource newResource(IRI predicate) {
        return newResource(newPredicate(predicate));
    }

    @Override
    public Resource newResource(String predicate) {
        return newResource(newPredicate(predicate));
    }

    @Override
    public Resource add(Property predicate, String value) {
        return add(predicate, newLiteral(value));
    }

    @Override
    public Resource add(Property predicate, Integer value) {
        return add(predicate, newLiteral(value));
    }

    @Override
    public Resource add(Property predicate, Boolean value) {
        return add(predicate, newLiteral(value));
    }

    @Override
    public Resource add(Property predicate, List<Node> objects) {
        for (Node object : objects) {
            add(predicate, newLiteral(object));
        }
        return this;
    }

    @Override
    public Resource add(String predicate, String value) {
        return add(newPredicate(predicate), value);
    }

    @Override
    public Resource add(String predicate, Integer value) {
        return add(newPredicate(predicate), value);
    }

    @Override
    public Resource add(String predicate, Boolean value) {
        return add(newPredicate(predicate), value);
    }

    @Override
    public Resource add(String predicate, Literal value) {
        return add(newPredicate(predicate), value);
    }

    @Override
    public Resource add(String predicate, IRI externalResource) {
        return add(newPredicate(predicate), externalResource);
    }

    @Override
    public Resource add(String predicate, List<Node> objects) {
        return add(newPredicate(predicate), objects);
    }

    @Override
    public Resource add(String predicate, Resource resource) {
        return add(newPredicate(predicate), resource);
    }

    @Override
    public Collection<Node> objects(String predicate) {
        return objects(newPredicate(predicate).id());
    }

    @Override
    public List<Triple> triples() {
        return new Triples(this, true).list();
    }

    @Override
    public List<Triple> properties() {
        return new Triples(this, false).list();
    }

    public Resource newSubject(Object subject) {
        return subject == null ? null :
                subject instanceof Resource ? (Resource) subject :
                        subject instanceof IRI ? new MemoryResource().id((IRI) subject) :
                                new MemoryResource().id(IRI.builder().curie(subject.toString()).build());
    }

    public Property newPredicate(Object predicate) {
        return predicate == null ? null :
                predicate instanceof Property ? (Property) predicate :
                        predicate instanceof IRI ? new MemoryProperty((IRI) predicate) :
                                new MemoryProperty(IRI.builder().curie(predicate.toString()).build());
    }

    public Node newObject(Object object) {
        return object == null ? null :
                object instanceof Literal ? (Node) object :
                        object instanceof IRI ? new MemoryResource().id((IRI) object) :
                                new MemoryLiteral(object);
    }

    public Literal newLiteral(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Literal) {
            return (Literal) value;
        }
        if (value instanceof Double) {
            return new MemoryLiteral(value).type(DOUBLE);
        }
        if (value instanceof Float) {
            return new MemoryLiteral(value).type(FLOAT);
        }
        if (value instanceof Long) {
            return new MemoryLiteral(value).type(LONG);
        }
        if (value instanceof Integer) {
            return new MemoryLiteral(value).type(INT);
        }
        if (value instanceof Boolean) {
            return new MemoryLiteral(value).type(BOOLEAN);
        }
        // untyped
        return new MemoryLiteral(value);
    }

    class Triples {

        private final List<Triple> triples;

        private final boolean recursive;

        public Triples(Resource resource, boolean recursive) {
            this.recursive = recursive;
            this.triples = unfold(resource);
        }

        public List<Triple> list() {
            return triples;
        }

        private List<Triple> unfold(Resource resource) {
            List<Triple> list = new ArrayList<Triple>();
            if (resource == null) {
                return list;
            }
            for (IRI pred : resource.predicates()) {
                for (Node obj : resource.objects(pred)) {
                    list.add(new MemoryTriple(resource, new MemoryProperty(pred), obj));
                    if (recursive && obj instanceof Resource) {
                        list.addAll(unfold((Resource) obj));
                    }
                }
            }
            return list;
        }
    }

}
