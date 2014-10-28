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
import org.xbib.rdf.Property;
import org.xbib.rdf.Identifiable;
import org.xbib.rdf.Literal;
import org.xbib.rdf.RDFNS;
import org.xbib.rdf.types.XSDResourceIdentifiers;

public final class MemoryFactory<S, P, O> implements XSDResourceIdentifiers {

    private final static Property TYPE = new MemoryProperty(RDFNS.RDF_TYPE);

    private final static Property LANG = new MemoryProperty(RDFNS.RDF_LANGUAGE);

    private final static transient MemoryFactory instance = new MemoryFactory();

    private MemoryFactory() {
    }

    public static <S, P, O> MemoryFactory<S, P, O> getInstance() {
        return instance;
    }

    public S newSubject(Object subject) {
        return subject instanceof Identifiable ? (S) subject :
                subject instanceof IRI ? (S) new MemoryNode().id((IRI) subject) :
                        (S) new MemoryResource().id(IRI.builder().curie(subject.toString()).build());
    }

    public P newPredicate(Object predicate) {
        return predicate == null ? null :
                predicate instanceof Property ? (P) predicate :
                        predicate instanceof IRI ? (P) new MemoryProperty((IRI) predicate) :
                                (P) new MemoryProperty(IRI.builder().curie(predicate.toString()).build());
    }

    public O newObject(Object object) {
        return object == null ? null :
                object instanceof Literal ? (O) object :
                        object instanceof IRI ? (O) new MemoryResource().id((IRI) object) :
                                (O) new MemoryLiteral(object);
    }

    public Literal newLiteral(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Literal) {
            return (Literal) value;
        }
        Literal l = new MemoryLiteral();
        if (value instanceof Double) {
            return l.type(DOUBLE).object(value);
        }
        if (value instanceof Float) {
            return l.type(FLOAT).object(value);
        }
        if (value instanceof Long) {
            return l.type(LONG).object(value);
        }
        if (value instanceof Integer) {
            return l.type(INT).object(value);
        }
        if (value instanceof Boolean) {
            return l.type(BOOLEAN).object(value);
        }
        // auto derive
        return l.object(value);
    }

    public Identifiable newBlankNode(String nodeID) {
        return new MemoryNode().id(IRI.builder().curie(nodeID).build());
    }

    public P rdfType() {
        return (P) TYPE;
    }

    public P rdfLang() {
        return (P) LANG;
    }

}
