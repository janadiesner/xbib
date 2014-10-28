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
import org.xbib.rdf.Node;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An in-memory identifiable node (including blank nodes)
 */
public class MemoryNode implements Identifiable, Node {

    private final static AtomicLong nodeID = new AtomicLong();

    private final static String GENID = "genid";

    private final static String PLACEHOLDER = "_:";

    private IRI id;

    public MemoryNode() {
    }

    public MemoryNode blank(String id) {
        id(IRI.builder().curie(GENID, id).build());
        return this;
    }

    public MemoryNode blank() {
        id(IRI.builder().curie(GENID, "b" + next()).build());
        return this;
    }

    @Override
    public MemoryNode id(IRI id) {
        this.id = id;
        return this;
    }

    @Override
    public IRI id() {
        return id;
    }

    public boolean isBlank() {
        return GENID.equals(id.getScheme());
    }

    @Override
    public String toString() {
        if (id == null) {
            blank();
        }
        return isBlank() ? PLACEHOLDER + id.getSchemeSpecificPart() : id.toString();
    }

    @Override
    public Object value() {
        return id;
    }

    public static void reset() {
        nodeID.set(0L);
    }

    public static long next() {
        return nodeID.incrementAndGet();
    }
}
