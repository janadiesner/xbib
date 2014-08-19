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
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.RDFNS;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.AbstractTripleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;

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
public class TurtleWriter<S extends Identifier, P extends Property, O extends Node, C extends ResourceContext<Resource<S,P,O>>>
        extends AbstractTripleWriter<S, P, O, C> {

    private final static Logger logger = LoggerFactory.getLogger(TurtleWriter.class.getName());

    private final Writer writer;

    private final static char LF = '\n';

    private final static char TAB = '\t';

    private boolean sameResource;

    private boolean sameProperty;

    private S lastSubject;

    private P lastPredicate;

    private Stack<IRI> embedded;

    private Stack<Triple<S, P, O>> triples;

    private Triple<S, P, O> triple;

    private boolean nsWritten;

    private StringBuilder namespaceBuilder;

    private StringBuilder sb;

    public TurtleWriter(Writer writer) {
        this.writer = writer;
        this.resourceContext = (C)new SimpleResourceContext();
        resourceContext.newResource();
        this.nsWritten = false;
        this.sameResource = false;
        this.sameProperty = false;
        this.triples = new Stack();
        this.embedded = new Stack();
        this.namespaceBuilder = new StringBuilder();
        this.sb = new StringBuilder();
    }

    public Writer getWriter() {
        return writer;
    }

    @Override
    public TurtleWriter<S, P, O, C> newIdentifier(IRI iri) {
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
    public TurtleWriter<S, P, O, C>  end() {
        return this;
    }

    @Override
    public TurtleWriter<S, P, O, C> triple(Triple triple) {
        resourceContext.getResource().add(triple);
        return this;
    }

    @Override
    public TurtleWriter<S, P, O, C> begin() {
        return this;
    }

    @Override
    public TurtleWriter<S, P, O, C> startPrefixMapping(String prefix, String uri) {
        namespaceContext.addNamespace(prefix, uri);
        return this;
    }

    @Override
    public TurtleWriter<S, P, O, C>  endPrefixMapping(String prefix) {
        return this;
    }

    @Override
    public void write(C resourceContext) throws IOException {
        for (Triple<S, P, O> spoTriple : resourceContext.getResource()) {
            writeTriple(spoTriple);
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
                String nsURI = entry.getValue().toString();
                if (!RDFNS.NS_URI.equals(nsURI)) {
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

    public void writeTriple(Triple<S, P, O> stmt) throws IOException {
        this.triple = stmt;
        S subject = stmt.subject();
        P predicate = stmt.predicate();
        O object = stmt.object();
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
                sb.append(';');
                sb.append(LF);
                writeIndent(embedded.size() + 1);
                writePredicate(predicate);
                writeObject(object);
            }
        } else {
            IRI iri = embedded.isEmpty() ? null : embedded.peek();
            boolean closeEmbedded = lastSubject != null
                    && lastSubject.isBlank()
                    && !subject.id().equals(iri);
            int n = embedded.indexOf(iri) - embedded.indexOf(subject.id());
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
                        writeIndent(1);
                        writeIndent(embedded.size());
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

    private void writeSubject(S subject) throws IOException {
        if (subject.id() == null) {
            sb.append("<> ");
            return;
        }
        if (!subject.isBlank()) {
            sb.append('<').append(subject.toString()).append("> ");
        }
        lastSubject = subject;
    }

    private final static String TYPE = RDFNS.NS_URI + "type";

    private void writePredicate(P predicate) throws IOException {
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

    private void writeObject(O object) throws IOException {
        if (object instanceof Resource) {
            Resource r = (Resource<S, P, O>) object;
            if (r.isBlank()) {
                openEmbeddedResource(r.id());
                sameResource = false;
                sameProperty = false;
            } else {
                writeURI(r.id());
            }
        } else if (object instanceof Literal) {
            writeLiteral((Literal<?>) object);
        } else if (object instanceof IdentifiableNode) {
            writeURI(((IdentifiableNode) object).id());
        } else {
            throw new IllegalArgumentException("unknown value class: "
                    + (object != null ? object.getClass() : "<null>"));
        }
    }

    private void openEmbeddedResource(IRI iri) throws IOException {
        triples.push(triple);
        embedded.push(iri);
        sb.append('[').append(LF);
        writeIndent(1);
    }

    private IRI closeEmbeddedResource() throws IOException {
        if (embedded.isEmpty()) {
            return null;
        }
        sb.append(LF);
        writeIndent(embedded.size());
        sb.append(']');
        Triple<S, P, O> t = triples.pop();
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
        String value = literal.nativeValue().toString();
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

    private boolean isPrefixStartChar(int c) {
        return Character.isLetter(c) || c >= 0x00C0 && c <= 0x00D6 || c >= 0x00D8 && c <= 0x00F6 || c >= 0x00F8 && c <= 0x02FF || c >= 0x0370 && c <= 0x037D || c >= 0x037F && c <= 0x1FFF || c >= 0x200C && c <= 0x200D || c >= 0x2070 && c <= 0x218F || c >= 0x2C00 && c <= 0x2FEF || c >= 0x3001 && c <= 0xD7FF || c >= 0xF900 && c <= 0xFDCF || c >= 0xFDF0 && c <= 0xFFFD || c >= 0x10000 && c <= 0xEFFFF;
    }

    private boolean isNameStartChar(int c) {
        return c == '_' || isPrefixStartChar(c);
    }

    private boolean isNameChar(int c) {
        return isNameStartChar(c) || Character.isDigit(c) || c == '-' || c == 0x00B7 || c >= 0x0300 && c <= 0x036F || c >= 0x203F && c <= 0x2040;
    }

    private boolean isPrefixChar(int c) {
        return isNameChar(c);
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

}
