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
package org.xbib.rdf.io.xml;

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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Write resource as XML to stream
 */
public class XmlWriter<C extends Context<Resource>>
        implements ContextWriter<C, Resource>, Triple.Builder, Closeable, Flushable {

    private final static Logger logger = LoggerFactory.getLogger(XmlWriter.class.getName());

    private final Writer writer;

    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    private C resourceContext;

    private String sortLangTag;

    public XmlWriter(Writer writer) {
        this.writer = writer;
    }

    public Writer getWriter() {
        return writer;
    }


    @Override
    public void close() throws IOException {
        // write last resource
        write(resourceContext);
    }

    public XmlWriter<C> setNamespaceContext(IRINamespaceContext context) {
        this.namespaceContext = context;
        return this;
    }

    public XmlWriter<C> setSortLanguageTag(String languageTag) {
        this.sortLangTag = languageTag;
        return this;
    }


    @Override
    public XmlWriter<C> newIdentifier(IRI iri) {
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
    public XmlWriter<C> begin() {
        return this;
    }

    @Override
    public XmlWriter<C> triple(Triple triple) {
        resourceContext.getResource().add(triple);
        return this;
    }

    @Override
    public XmlWriter<C> end() {
        return this;
    }

    @Override
    public XmlWriter<C> startPrefixMapping(String prefix, String uri) {
        namespaceContext.addNamespace(prefix, uri);
        return this;
    }

    @Override
    public XmlWriter<C> endPrefixMapping(String prefix) {
        // we don't remove name spaces. It's troubling RDF serializations.
        //namespaceContext.removeNamespace(prefix);
        return this;
    }

    @Override
    public void write(C resourceContext) throws IOException {
        Resource resource = resourceContext.getResource();
        try {
            XMLEventWriter xew = outputFactory.createXMLEventWriter(writer);
            IRI resourceURI = resource.id();
            String nsPrefix = resourceURI.getScheme();
            String name = resourceURI.getSchemeSpecificPart();
            String nsURI = namespaceContext.getNamespaceURI(nsPrefix);
            writeResource(xew, resource, new QName(nsURI, name, nsPrefix));
            xew.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private void writeResource(XMLEventConsumer consumer, Resource resource, QName parent)
            throws XMLStreamException {
        boolean startElementWritten = false;
        List<Triple> triples = resource.properties();
        for (Triple triple : triples) {
            if (!startElementWritten) {
                if (parent != null) {
                    consumer.add(eventFactory.createStartElement(parent, null, null));
                }
                startElementWritten = true;
            }
            write(consumer, triple);
        }
        if (!startElementWritten) {
            if (parent != null) {
                consumer.add(eventFactory.createStartElement(parent, null, null));
            }
        }
        if (parent != null) {
            consumer.add(eventFactory.createEndElement(parent, null));
        }
    }

    private void write(XMLEventConsumer consumer, Triple triple)
            throws XMLStreamException {
        IRI predicate = triple.predicate();
        Node object = triple.object();
        String nsPrefix = predicate.getScheme();
        String name = predicate.getSchemeSpecificPart();
        String nsURI = namespaceContext.getNamespaceURI(nsPrefix);
        if (object instanceof Resource) {
            writeResource(consumer, (Resource) object, new QName(nsURI, name, nsPrefix));
        } else if (object instanceof Literal) {
            String literal = ((Literal) object).object().toString();
            consumer.add(eventFactory.createStartElement(nsPrefix, nsURI, name));
            consumer.add(eventFactory.createCharacters(literal));
            consumer.add(eventFactory.createEndElement(nsPrefix, nsURI, name));
        } else {
            throw new XMLStreamException("can't write object class: " + object.getClass().getName());
        }

    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
