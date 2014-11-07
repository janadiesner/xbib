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
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

/**
 * A simple triple
 */
public class MemoryTriple implements Triple, Comparable<Triple> {

    private Resource subject;

    private IRI predicate;

    private Node object;

    /**
     * Create a new triple
     *
     * @param subject   subject
     * @param predicate predicate
     * @param object    object
     */
    public MemoryTriple(Resource subject, IRI predicate, Node object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public Triple subject(Resource subject) {
        this.subject = subject;
        return this;
    }

    @Override
    public Resource subject() {
        return subject;
    }

    @Override
    public Triple predicate(IRI predicate) {
        this.predicate = predicate;
        return null;
    }

    @Override
    public IRI predicate() {
        return predicate;
    }

    @Override
    public Triple object(Node object) {
        this.object = object;
        return this;
    }

    @Override
    public Node object() {
        return object;
    }

    @Override
    public String toString() {
        return (subject != null ? subject : " <null>")
                + (predicate != null ? " " + predicate : " <null>")
                + (object != null ? " " + object : " <null>");
    }

    @Override
    public int hashCode() {
        return (subject != null ? subject.hashCode() : 0)
                + (predicate != null ? predicate.hashCode() : 0)
                + (object != null ? object.hashCode() : 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((Triple) obj) == 0;
    }

    @Override
    public int compareTo(Triple triple) {
        return toString().compareTo(triple.toString());
    }
}
