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
import org.xbib.rdf.context.ResourceContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A simple resource is a sequence of properties and of associated resources.
 */
public class MemoryResource<S extends Identifiable, P extends Property, O extends Node>
        extends MemoryNode
        implements Resource<S, P, O>, Comparable<Resource<S, P, O>> {

    private transient final MemoryFactory<S, P, O> memoryFactory = MemoryFactory.getInstance();

    private ResourceContext context = new MemoryResourceContext();

    private MultiMap<P, Node> attributes = new LinkedHashMultiMap<P, Node>();

    private Map<IRI, O> resources = new LinkedHashMap<IRI, O>();

    private S subject;

    private boolean deleted;

    @Override
    public MemoryResource<S, P, O> id(IRI identifier) {
        super.id(identifier);
        this.subject = (S) new MemoryNode().id(identifier);
        return this;
    }

    @Override
    public Resource<S, P, O> subject(S subject) {
        this.subject = subject;
        return this;
    }

    public Resource<S, P, O> subject(IRI subject) {
        this.subject = (S) new MemoryNode().id(subject);
        return this;
    }

    @Override
    public S subject() {
        return subject;
    }

    @Override
    public Resource<S, P, O> add(Triple<S, P, O> triple) {
        if (triple == null) {
            return this;
        }
        IRI id = triple.subject().id();
        if (id == null || id.equals(id())) {
            add(triple.predicate(), triple.object());
        } else {
            Resource<S, P, O> r = (Resource<S, P, O>) resources.get(id);
            if (r != null) {
                return r.add(triple);
            } else {
                // continue with new resource with new subject
                return new MemoryResource<S, P, O>().id(id).add(triple);
            }
        }
        return this;
    }

    @Override
    public Resource<S, P, O> add(P predicate, O object) {
        attributes.put(predicate, object);
        if (object instanceof MemoryNode) {
            resources.put(((MemoryNode) object).id(), object);
        }
        return this;
    }

    @Override
    public Resource<S, P, O> add(P predicate, IRI iri) {
        return add(predicate, (O) new MemoryNode().id(iri));
    }

    @Override
    public Resource<S, P, O> add(P predicate, Literal<O> literal) {
        // drop null literals silently
        if (literal != null) {
            attributes.put(predicate, literal);
        }
        return this;
    }

    @Override
    public Resource<S, P, O> add(P predicate, Resource<S, P, O> resource) {
        if (resource == null) {
            return this;
        }
        if (resource.id() == null) {
            resource.id(super.id());
            Resource<S, P, O> r = newResource(predicate);
            for (Triple<S, P, O> triple : resource) {
                r.add(triple);
            }
        } else {
            attributes.put(predicate, resource);
        }
        return this;
    }

    @Override
    public Resource<S, P, O> a(IRI externalResource) {
        add("rdf:type", externalResource);
        return this;
    }

    @Override
    public Set<P> predicates() {
        return attributes.keySet();
    }

    @Override
    public Collection<O> objects(P predicate) {
        return (Collection<O>) attributes.get(predicate);
    }

    @Override
    public O literal(P predicate) {
        return attributes.containsKey(predicate) ?
                (O) attributes.get(predicate).iterator().next() : null;
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
    public void compactPredicate(P predicate) {
        Stream<Node> stream = resources(predicate);
        Iterator<Node> it = stream.iterator();
        Resource<S, P, O> resource = (Resource<S, P, O>) it.next();
        if (!it.hasNext()) {
            Collection<Node> newResource = new LinkedList();
            for (O o : objects(predicate)) {
                if (o instanceof Literal) {
                    newResource.add(o);
                } else if (o instanceof MemoryNode) {
                    if (!((MemoryNode) o).isBlank()) {
                        newResource.add(o);
                    }
                }
            }
            newResource.addAll(new LinkedList(resource.objects(predicate)));
            attributes.removeAll(predicate);
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
    public Resource<S, P, O> setDeleted(boolean delete) {
        this.deleted = delete;
        return this;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return "<" + (super.id() != null ? super.id() : "") + ">";
    }

    @Override
    public Stream<Node> resources(P predicate) {
        return attributes.get(predicate).stream().filter(n -> n instanceof Resource);
    }

    @Override
    public MemoryResource<S, P, O> context(ResourceContext context) {
        this.context = context;
        return this;
    }

    @Override
    public ResourceContext context() {
        return context;
    }

    @Override
    public Resource<S, P, O> newResource(P predicate) {
        IRI blank = new MemoryNode().blank().id();
        Resource<S, P, O> r = new MemoryResource<S,P,O>().id(blank);
        resources.put(blank, (O) r);
        attributes.put(predicate, r);
        return r;
    }

    @Override
    public Resource<S, P, O> add(P predicate, String value) {
        return add(predicate, memoryFactory.newLiteral(value));
    }

    @Override
    public Resource<S, P, O> add(P predicate, Integer value) {
        return add(predicate, memoryFactory.newLiteral(value));
    }

    @Override
    public Resource<S, P, O> add(P predicate, Collection literals) {
        for (Object object : literals) {
            add(predicate, memoryFactory.newLiteral(object));
        }
        return this;
    }

    @Override
    public Resource<S, P, O> add(String predicate, String value) {
        return add(memoryFactory.newPredicate(predicate), value);
    }

    @Override
    public Resource<S, P, O> add(String predicate, Integer value) {
        return add(memoryFactory.newPredicate(predicate), value);
    }

    @Override
    public Resource<S, P, O> add(String predicate, Literal value) {
        return add(memoryFactory.newPredicate(predicate), value);
    }

    @Override
    public Resource<S, P, O> add(String predicate, IRI externalResource) {
        return add(memoryFactory.newPredicate(predicate), externalResource);
    }

    @Override
    public Resource<S, P, O> add(String predicate, Collection literals) {
        return add(memoryFactory.newPredicate(predicate), literals);
    }

    @Override
    public Resource<S, P, O> newResource(IRI predicate) {
        return newResource(memoryFactory.newPredicate(predicate));
    }

    @Override
    public Resource<S, P, O> newResource(String predicate) {
        return newResource(memoryFactory.newPredicate(predicate));
    }

    @Override
    public Resource<S, P, O> add(String predicate, Resource<S, P, O> resource) {
        return add(memoryFactory.newPredicate(predicate), resource);
    }

    @Override
    public Collection<O> objects(String predicate) {
        return objects(memoryFactory.newPredicate(predicate));
    }

    @Override
    public O literal(String predicate) {
        return literal(memoryFactory.newPredicate(predicate));
    }

    @Override
    public Iterator<Triple<S, P, O>> iterator() {
        return new TripleIterator(this, true);
    }

    @Override
    public Iterator<Triple<S, P, O>> propertyIterator() {
        return new TripleIterator(this, false);
    }

    @Override
    public int compareTo(Resource<S, P, O> o) {
        return id() == null ? -1 : id().toString().compareTo(o.id().toString());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (id() != null ? id().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    public Resource<S, P, O> type(IRI type) {
        add(memoryFactory.rdfType(), type);
        return this;
    }

    public IRI type() {
        Collection<Node> c = attributes.get(memoryFactory.rdfType());
        if (c != null) {
            return ((Identifiable)c.iterator().next()).id();
        }
        return null;
    }

    public Resource<S, P, O> language(String lang) {
        add(memoryFactory.rdfLang(), lang);
        return this;
    }

    public String language() {
        Collection<Node> c = attributes.get(memoryFactory.rdfLang());
        return c != null ? c.iterator().hasNext() ?
                c.iterator().next().toString()
                : null : null;
    }

    class TripleIterator<S extends Identifiable, P extends Property, O extends Node>
            implements Iterator<Triple> {

        private final LinkedList<Triple> triples;
        private final boolean includeResources;

        public TripleIterator(Resource<S, P, O> resource, boolean includeResources) {
            this.includeResources = includeResources;
            this.triples = unfold(resource);
        }

        @Override
        public boolean hasNext() {
            return !triples.isEmpty();
        }

        @Override
        public Triple next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return triples.poll();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private LinkedList<Triple> unfold(Resource<S, P, O> resource) {
            LinkedList<Triple> list = new LinkedList();
            if (resource == null) {
                return list;
            }
            S subj = resource.subject();
            for (P pred : resource.predicates()) {
                for (O obj : resource.objects(pred)) {
                    list.offer(new MemoryTriple(subj, pred, obj));
                    if (includeResources && obj instanceof Resource) {
                        list.addAll(unfold((Resource<S, P, O>) obj));
                    }
                }
            }
            return list;
        }

    }

}
