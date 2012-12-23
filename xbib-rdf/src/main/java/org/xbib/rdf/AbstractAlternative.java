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
package org.xbib.rdf;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.xbib.iri.IRI;

/**
 * The rdf:Alt class is the class of RDF 'Alternative' containers.
 * It is a subclass of rdfs:Container. Whilst formally it is no different
 * from an rdf:Seq or an rdf:Bag, the rdf:Alt class is used conventionally
 * to indicate to a human reader that typical processing will be to select
 * one of the members of the container. The first member of the container,
 * i.e. the value of the rdf:_1 property, is the default choice.
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 * @param <S>
 * @param <P>
 * @param <O>
 */
public abstract class AbstractAlternative<S extends Resource<?,?,?>, P extends Property, O extends Literal<?>>
        extends AbstractResource<S, P, O> implements Resource<S, P, O>, Comparable<Resource<S, P, O>> {

    public AbstractAlternative() {
        super();
    }

    protected AbstractAlternative(IRI identifier) {
        super(identifier);
    }

    @Override
    public Multimap<P, O> newProperties() {
        return LinkedHashMultimap.create();
    }

    @Override
    public Multimap<P, Resource<S, P, O>> newResources() {
        return LinkedHashMultimap.create();
    }
}
