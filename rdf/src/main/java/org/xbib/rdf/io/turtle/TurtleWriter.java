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
package org.xbib.rdf.io.turtle;

import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.context.ResourceContextWriter;
import org.xbib.rdf.Property;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.memory.MemoryResourceContext;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Stack;

/**
 * Write RDF Turtle
 * <p>
 * See <a href="http://www.w3.org/TeamSubmission/turtle/">Turtle - Terse RDF
 * Triple Language</a>
 */
public class TurtleWriter<C extends ResourceContext<Resource>>
        implements ResourceContextWriter<C, Resource>, Triple.Builder, Closeable, Flushable {

    private final static Logger logger = LoggerFactory.getLogger(TurtleWriter.class.getName());

    private final Writer writer;

    private final static char LF = '\n';

    private final static char TAB = '\t';

    private final static String TYPE = RdfConstants.NS_URI + "type";

    private boolean sameResource;

    private boolean sameProperty;

    private Resource lastSubject;

    private Property lastPredicate;

    private Node lastObject;

    private Stack<Resource> embedded;

    private Stack<Triple> triples;

    private Triple triple;

    private boolean nsWritten;

    private StringBuilder namespaceBuilder;

    private StringBuilder sb;

    public TurtleWriter(Writer writer) {
        this.writer = writer;
        this.resourceContext = (C)new MemoryResourceContext();
        resourceContext.newResource();
        this.nsWritten = false;
        this.sameResource = false;
        this.sameProperty = false;
        this.triples = new Stack<Triple>();
        this.embedded = new Stack<Resource>();
        this.namespaceBuilder = new StringBuilder();
        this.sb = new StringBuilder();
    }

    public Writer getWriter() {
        return writer;
    }

    protected IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    protected C resourceContext;

    private String sortLangTag;

    @Override
    public void close() throws IOException {
        // write last resource
        write(resourceContext);
    }

    public TurtleWriter<C>  setNamespaceContext(IRINamespaceContext context) {
        this.namespaceContext = context;
        return this;
    }

    public TurtleWriter<C>  setSortLanguageTag(String languageTag) {
        this.sortLangTag = languageTag;
        return this;
    }

    @Override
    public TurtleWriter<C> newIdentifier(IRI iri) {
        if (iri != null && !iri.equals(resourceContext.getResource().id())) {
            try {
                write(resourceContext);
                resourceContext.newResource();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            resourceContext.getResource().id(iri);
        }
        return this;
    }

    @Override
    public TurtleWriter<C>  end() {
        return this;
    }

    @Override
    public TurtleWriter<C> triple(Triple triple) {
        resourceContext.getResource().add(triple);
        return this;
    }

    @Override
    public TurtleWriter<C> begin() {
        return this;
    }

    @Override
    public TurtleWriter<C> startPrefixMapping(String prefix, String uri) {
        namespaceContext.addNamespace(prefix, uri);
        return this;
    }

    @Override
    public TurtleWriter<C>  endPrefixMapping(String prefix) {
        return this;
    }

    @Override
    public void write(C resourceContext) throws IOException {
        for (Triple triple : resourceContext.getResource().triples()) {
            writeTriple(triple);
        }
        while (!embedded.isEmpty()) {
            closeEmbeddedResource();
        }
        if (sb.length() > 0) {
            sb.append('.').append(LF);
        }
        if (writer != null) {
            writer.write(namespaceBuilder.toString());
            writer.write(sb.toString());
        }
        namespaceBuilder.setLength(0);
        sb.setLength(0);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    public TurtleWriter writeNamespaces() throws IOException {
        if (namespaceContext == null) {
            return this;
        }
        nsWritten = false;
        for (Map.Entry<String, String> entry : namespaceContext.getNamespaces().entrySet()) {
            if (entry.getValue().length() > 0) {
                String nsURI = entry.getValue();
                if (!RdfConstants.NS_URI.equals(nsURI)) {
                    namespaceBuilder.append("@prefix ")
                            .append(entry.getKey())
                            .append(": <")
                            .append(encodeURIString(nsURI))
                            .append("> .")
                            .append(LF);
                    nsWritten = true;
                }
            }
        }
        if (nsWritten) {
            namespaceBuilder.append(LF);
        }
        return this;
    }

    public void writeTriple(Triple stmt) throws IOException {
        this.triple = stmt;
        Resource subject = stmt.subject();
        Property predicate = stmt.predicate();
        Node object = stmt.object();
        if (subject == null || predicate == null) {
            return;
        }
        boolean sameSubject = subject.equals(lastSubject);
        boolean samePredicate = predicate.equals(lastPredicate);
        if (sameSubject) {
            if (samePredicate) {
                sb.append(", ");
                writeObject(object);
            } else {
                if (!(lastObject instanceof Resource && ((Resource) lastObject).isEmbedded())) {
                    sb.append(';').append(LF);
                    writeIndent(1);
                }
                writeIndent(embedded.size());
                writePredicate(predicate);
                writeObject(object);
            }
        } else {
            // un-indent
            Resource r = embedded.isEmpty() ? null : embedded.peek();
            boolean closeEmbedded = lastSubject != null
                    && lastSubject.isEmbedded()
                    && !subject.equals(r);
            int n = embedded.indexOf(r) - embedded.indexOf(subject);
            if (closeEmbedded) {
                for (int i = 0; i < n; i++) {
                    closeEmbeddedResource();
                }
            }
            if (lastSubject != null) {
                if (sameResource) {
                    if (sameProperty) {
                        sb.append(',');
                    } else {
                        sb.append(';').append(LF);
                        writeIndent(embedded.size() + 1);
                    }
                } else {
                    if (sameProperty) {
                        sb.append(";").append(LF);
                        writeIndent(1);
                    } else if (closeEmbedded) {
                        sb.append(";").append(LF);
                        writeIndent(1);
                    }
                    writeIndent(embedded.size());
                }
            }
            if (!sameResource) {
                writeSubject(subject);
            }
            if (!sameProperty) {
                writePredicate(predicate);
            }
            writeObject(object);
        }
    }

    private void writeSubject(Resource subject) throws IOException {
        if (subject.id() == null) {
            sb.append("<> ");
            return;
        }
        if (!subject.isEmbedded()) {
            sb.append('<').append(subject.toString()).append("> ");
        }
        lastSubject = subject;
    }

    private void writePredicate(Property predicate) throws IOException {
        if (predicate.id() == null) {
            sb.append("<> ");
            return;
        }
        String p = predicate.toString();
        if ("rdf:type".equals(p) || TYPE.equals(p)) {
            sb.append("a ");
        } else {
            writeURI(predicate.id());
            sb.append(" ");
        }
        lastPredicate = predicate;
    }

    private void writeObject(Node object) throws IOException {
        if (object instanceof Resource) {
            Resource r = (Resource) object;
            if (r.isEmbedded()) {
                openEmbeddedResource(r);
                sameResource = false;
                sameProperty = false;
            } else {
                writeURI(r.id());
            }
        } else if (object instanceof Literal) {
            writeLiteral((Literal) object);
        } else {
            throw new IllegalArgumentException("unknown value class: "
                    + (object != null ? object.getClass() : "<null>"));
        }
        lastObject = object;
    }

    private void openEmbeddedResource(Resource r) throws IOException {
        triples.push(triple);
        embedded.push(r);
        sb.append('[').append(LF);
        writeIndent(1);
    }

    private Resource closeEmbeddedResource() throws IOException {
        if (embedded.isEmpty()) {
            return null;
        }
        sb.append(LF);
        writeIndent(embedded.size());
        sb.append(']');
        Triple t = triples.pop();
        lastSubject = t.subject();
        lastPredicate = t.predicate();
        sameResource = lastSubject.equals(triple.subject());
        sameProperty = lastPredicate.equals(triple.predicate());
        return embedded.pop();
    }

    private void writeURI(IRI uri) throws IOException {
        String abbrev = namespaceContext.compact(uri);
        if (!abbrev.equals(uri.toString())) {
            sb.append(abbrev);
            return;
        }
        if (namespaceContext.getNamespaceURI(uri.getScheme()) != null) {
            sb.append(uri.toString());
            return;
        }
        sb.append('<').append(encodeURIString(uri.toString())).append('>');
    }

    private void writeLiteral(Literal literal) throws IOException {
        literal = processSortLanguage(literal);
        String value = literal.object().toString();
        if (value.indexOf('\n') > 0 || value.indexOf('\r') > 0 || value.indexOf('\t') > 0) {
            sb.append("\"\"\"")
                    .append(encodeLongString(value))
                    .append("\"\"\"");
        } else {
            sb.append('\"')
                    .append(encodeString(value))
                    .append('\"');
        }
        if (literal.type() != null) {
            sb.append("^^").append(literal.type().toString());
        } else if (literal.language() != null) {
            sb.append('@').append(literal.language());
        }
    }

    private void writeIndent(int indentLevel) throws IOException {
        for (int i = 0; i < indentLevel; i++) {
            sb.append(TAB);
        }
    }

    private String encodeString(String s) {
        s = gsub("\\", "\\\\", s);
        s = gsub("\t", "\\t", s);
        s = gsub("\n", "\\n", s);
        s = gsub("\r", "\\r", s);
        s = gsub("\"", "\\\"", s);
        return s;
    }

    private String encodeLongString(String s) {
        s = gsub("\\", "\\\\", s);
        s = gsub("\"", "\\\"", s);
        return s;
    }

    private String encodeURIString(String s) {
        s = gsub("\\", "\\\\", s);
        s = gsub(">", "\\>", s);
        return s;
    }

    private String gsub(String olds, String news, String text) {
        if (olds == null || olds.length() == 0) {
            return text;
        }
        if (text == null) {
            return null;
        }
        int oldsIndex = text.indexOf(olds);
        if (oldsIndex == -1) {
            return text;
        }
        StringBuilder buf = new StringBuilder(text.length());
        int prevIndex = 0;
        while (oldsIndex >= 0) {
            buf.append(text.substring(prevIndex, oldsIndex));
            buf.append(news);
            prevIndex = oldsIndex + olds.length();
            oldsIndex = text.indexOf(olds, prevIndex);
        }
        buf.append(text.substring(prevIndex));
        return buf.toString();
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
