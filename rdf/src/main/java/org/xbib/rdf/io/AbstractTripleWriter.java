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
package org.xbib.rdf.io;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.AbstractResourceContextWriter;
import org.xbib.rdf.context.ResourceContext;

import java.io.IOException;

public abstract class AbstractTripleWriter<S extends Identifier, P extends Property, O extends Node, C extends ResourceContext<Resource<S,P,O>>>
    extends AbstractResourceContextWriter<C, Resource<S,P,O>> implements TripleWriter<S, P, O, C> {

    protected IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    protected C resourceContext;

    private String sortLangTag;

    @Override
    public void close() throws IOException {
        // write last resource
        write(resourceContext);
        flush();
    }

    public AbstractTripleWriter<S, P, O, C> setNamespaceContext(IRINamespaceContext context) {
        this.namespaceContext = context;
        return this;
    }

    public AbstractTripleWriter<S, P, O, C> setSortLanguageTag(String languageTag) {
        this.sortLangTag = languageTag;
        return this;
    }

    /**
     *
     * Process a literal according to given sort language (e.g. mechanical word order, sort area).
     *
     * see http://www.w3.org/International/articles/language-tags/
     *
     * @param literal the literal
     * @return the process literal
     */
    protected Literal processSortLanguage(Literal literal) {
        if (literal == null) {
            return null;
        }
        if (sortLangTag == null) {
            return literal;
        }
        // we assume we have only one sort language. Search for '@' symbol.
        String value = literal.object().toString();
        // ignore if on position 0
        int pos = value.indexOf(" @");
        if (pos == 0) {
            literal.object(value.substring(1));
        } else if (pos > 0) {
            literal.object('\u0098' + value.substring(0, pos + 1) + '\u009c' + value.substring(pos + 2)).language(sortLangTag);
        }
        return literal;
    }

}
