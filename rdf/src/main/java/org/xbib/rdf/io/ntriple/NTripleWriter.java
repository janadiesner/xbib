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
package org.xbib.rdf.io.ntriple;

import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.AbstractTripleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;

import java.io.IOException;
import java.io.Writer;

/**
 * NTriple writer
 */
public class NTripleWriter<S extends Identifier, P extends Property, O extends Node, C extends ResourceContext<Resource<S,P,O>>>
        extends AbstractTripleWriter<S, P, O, C> {

    private final static Logger logger = LoggerFactory.getLogger(NTripleWriter.class.getName());

    private final static char LF = '\n';

    private final Writer writer;

    public NTripleWriter(Writer writer) {
        this.writer = writer;
        this.resourceContext = (C)new SimpleResourceContext();
        resourceContext.newResource();
    }

    public Writer getWriter() {
        return writer;
    }

    @Override
    public NTripleWriter<S, P, O, C>  newIdentifier(IRI iri) {
        if (!iri.equals(resourceContext.getResource().id())) {
            try {
                write(resourceContext);
                resourceContext.newResource();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        resourceContext.getResource().id(iri);
        return this;
    }

    @Override
    public NTripleWriter<S, P, O, C> begin() {
        return this;
    }

    @Override
    public NTripleWriter<S, P, O, C> triple(Triple<S, P, O> triple) {
        resourceContext.getResource().add(triple);
        return this;
    }

    @Override
    public NTripleWriter<S, P, O, C> end() {
        return this;
    }

    @Override
    public NTripleWriter<S, P, O, C> startPrefixMapping(String prefix, String uri) {
        namespaceContext.addNamespace(prefix, uri);
        return this;
    }

    @Override
    public NTripleWriter<S, P, O, C> endPrefixMapping(String prefix) {
        // we don't remove name spaces. It's troubling RDF serializations.
        //namespaceContext.removeNamespace(prefix);
        return this;
    }

    @Override
    public void write(C resourceContext) throws IOException {
        if (resourceContext.getResource() != null) {
            StringBuilder sb = new StringBuilder();
            for (Triple<S, P, O> t : resourceContext.getResource()) {
                sb.append(writeStatement(t));
            }
            writer.write(sb.toString());
        } else if (resourceContext.getResources() != null) {
            for (Resource<S, P, O> resource : resourceContext.getResources()) {
                StringBuilder sb = new StringBuilder();
                for (Triple<S, P, O> t : resource) {
                    sb.append(writeStatement(t));
                }
                writer.write(sb.toString());
            }
        }
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    public String writeStatement(Triple<S, P, O> stmt) throws IOException {
        S subj = stmt.subject();
        P pred = stmt.predicate();
        O obj = stmt.object();
        return new StringBuilder().append(writeSubject(subj)).append(" ").append(writePredicate(pred)).append(" ").append(writeObject(obj)).append(" .").append(LF).toString();
    }

    public String writeSubject(S subject) {
        return subject.isBlank() ?
                subject.toString() :
                "<" + escape(subject.toString()) + ">";
    }

    public String writePredicate(P predicate) {
        /*if (predicate.id().getScheme() == null && nullPredicate !=null) {
            IRI iri = IRI.builder()
                    .scheme(nullPredicate.getScheme())
                    .host(nullPredicate.getHost())
                    .path(nullPredicate.getPath() + "/" + predicate.id().getSchemeSpecificPart())
                    .build();
            return "<" + escape(iri.toString()) + ">";
        }*/
        return "<" + escape(predicate.id().toString()) + ">";
    }

    public String writeObject(O object) {
        if (object instanceof Resource) {
            S subject = ((Resource<S, P, O>) object).subject();
            return writeSubject(subject);
        } else if (object instanceof Literal) {
            Literal<?> value = (Literal<?>) object;
            value = processSortLanguage(value);
            String s = "\"" + escape(value.object().toString()) + "\"";
            String lang = value.language();
            IRI type = value.type();
            if (lang != null) {
                return s + "@" + lang;
            }
            if (type != null) {
                return s + "^^<" + escape(type.toString()) + ">";
            }
            return s;
        } else if (object instanceof IdentifiableNode) {
            IdentifiableNode node = (IdentifiableNode) object;
            return node.isBlank() ?
                    node.toString() :
                    "<" + escape(node.toString()) + ">";
        }
        return "<???>";
    }

    private String escape(String buffer) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buffer.length(); i++) {
            char ch = buffer.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch >= 32 && ch <= 126) {
                        sb.append(ch);
                    } else {
                        sb.append("\\u");
                        String hexstr = Integer.toHexString(ch).toUpperCase();
                        int pad = 4 - hexstr.length();
                        for (; pad > 0; pad--) {
                            sb.append("0");
                        }
                        sb.append(hexstr);
                    }
            }
        }
        return sb.toString();
    }

}
