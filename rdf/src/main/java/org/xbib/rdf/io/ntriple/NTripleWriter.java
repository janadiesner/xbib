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
import org.xbib.rdf.Context;
import org.xbib.rdf.ContextWriter;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.memory.MemoryContext;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * NTriple writer
 */
public class NTripleWriter<C extends Context<Resource>>
        implements ContextWriter<C, Resource>, Triple.Builder, Closeable, Flushable {

    private final static Logger logger = LoggerFactory.getLogger(NTripleWriter.class.getName());

    private final static char LF = '\n';

    private final Writer writer;

    private IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    private C resourceContext;

    private String sortLangTag;


    public NTripleWriter(Writer writer) {
        this.writer = writer;
        this.resourceContext = (C) new MemoryContext();
        resourceContext.newResource();
    }

    public Writer getWriter() {
        return writer;
    }

    @Override
    public void close() throws IOException {
        // write last resource
        write(resourceContext);
    }

    public NTripleWriter<C> setNamespaceContext(IRINamespaceContext context) {
        this.namespaceContext = context;
        return this;
    }

    public NTripleWriter<C> setSortLanguageTag(String languageTag) {
        this.sortLangTag = languageTag;
        return this;
    }

    @Override
    public NTripleWriter<C> newIdentifier(IRI iri) {
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
    public NTripleWriter<C> begin() {
        return this;
    }

    @Override
    public NTripleWriter<C> triple(Triple triple) {
        resourceContext.getResource().add(triple);
        return this;
    }

    @Override
    public NTripleWriter<C> end() {
        return this;
    }

    @Override
    public NTripleWriter<C> startPrefixMapping(String prefix, String uri) {
        namespaceContext.addNamespace(prefix, uri);
        return this;
    }

    @Override
    public NTripleWriter<C> endPrefixMapping(String prefix) {
        // we don't remove name spaces. It's troubling RDF serializations.
        //namespaceContext.removeNamespace(prefix);
        return this;
    }

    @Override
    public void write(C resourceContext) throws IOException {
        if (resourceContext.getResource() != null) {
            StringBuilder sb = new StringBuilder();
            for (Triple t : resourceContext.getResource().triples()) {
                sb.append(writeStatement(t));
            }
            writer.write(sb.toString());
        } else if (resourceContext.getResources() != null) {
            for (Resource resource : resourceContext.getResources()) {
                StringBuilder sb = new StringBuilder();
                for (Triple t : resource.triples()) {
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

    public String writeStatement(Triple stmt) throws IOException {
        Resource subj = stmt.subject();
        IRI pred = stmt.predicate();
        Node obj = stmt.object();
        return writeSubject(subj) + " " + writePredicate(pred) + " " + writeObject(obj) + " ." + LF;
    }

    public String writeSubject(Resource subject) {
        return subject.isEmbedded() ?
                subject.toString() :
                "<" + escape(subject.toString()) + ">";
    }

    public String writePredicate(IRI predicate) {
        return "<" + escape(predicate.toString()) + ">";
    }

    public String writeObject(Node object) {
        if (object instanceof Resource) {
            Resource subject = (Resource) object;
            return writeSubject(subject);
        } else if (object instanceof Literal) {
            Literal value = (Literal) object;
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


    /**
     * Process a literal according to given sort language (e.g. mechanical word order, sort area).
     * <p>
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
