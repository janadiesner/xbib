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
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.AbstractTripleWriter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * Write resource as XML to stream
 */
public class XmlWriter<S extends Identifier, P extends Property, O extends Node, C extends ResourceContext<Resource<S,P,O>>>
        extends AbstractTripleWriter<S, P, O, C> {

    private final static Logger logger = LoggerFactory.getLogger(XmlWriter.class.getName());

    private final Writer writer;

    private NamespaceContext context = IRINamespaceContext.getInstance();

    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    public XmlWriter(Writer writer) {
       this.writer = writer;
    }

    public Writer getWriter() {
        return writer;
    }

    @Override
    public XmlWriter<S, P, O, C>  newIdentifier(IRI iri) {
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
    public XmlWriter<S, P, O, C> begin() {
        return this;
    }

    @Override
    public XmlWriter<S, P, O, C> triple(Triple<S, P, O> triple) {
        resourceContext.getResource().add(triple);
        return this;
    }

    @Override
    public XmlWriter<S, P, O, C> end() {
        return this;
    }

    @Override
    public XmlWriter<S, P, O, C> startPrefixMapping(String prefix, String uri) {
        namespaceContext.addNamespace(prefix, uri);
        return this;
    }

    @Override
    public XmlWriter<S, P, O, C> endPrefixMapping(String prefix) {
        // we don't remove name spaces. It's troubling RDF serializations.
        //namespaceContext.removeNamespace(prefix);
        return this;
    }

    public XmlWriter setNamespaceContext(NamespaceContext context) {
        this.context = context;
        return this;
    }

    @Override
    public void write(C resourceContext) throws IOException {
        Resource<S,P,O> resource = resourceContext.getResource();
        try {
            XMLEventWriter xew = outputFactory.createXMLEventWriter(writer);
            IRI resourceURI = resource.id();
            String nsPrefix = resourceURI.getScheme();
            String name = resourceURI.getSchemeSpecificPart();
            String nsURI = context.getNamespaceURI(nsPrefix);
            writeResource(xew, resource, new QName(nsURI, name, nsPrefix));
            xew.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private void writeResource(XMLEventConsumer consumer, Resource<S, P, O> resource, QName parent)
            throws XMLStreamException {
        boolean startElementWritten = false;
        Iterator<Triple<S, P, O>> it = resource.propertyIterator();
        while (it.hasNext()) {
            Triple<S, P, O> triple = it.next();
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

    private void write(XMLEventConsumer consumer, Triple<S, P, O> triple)
            throws XMLStreamException {
        P predicate = triple.predicate();
        O object = triple.object();
        String nsPrefix = predicate.id().getScheme();
        String name = predicate.id().getSchemeSpecificPart();
        String nsURI = context.getNamespaceURI(nsPrefix);
        if (object instanceof Resource) {
            writeResource(consumer, (Resource<S, P, O>) object, new QName(nsURI, name, nsPrefix));
        } else if (object instanceof Literal) {
            String literal = ((Literal) object).object().toString();
            consumer.add(eventFactory.createStartElement(nsPrefix, nsURI, name));
            consumer.add(eventFactory.createCharacters(literal));
            consumer.add(eventFactory.createEndElement(nsPrefix, nsURI, name));
        } else {
            throw new XMLStreamException("can't write object class: " + object.getClass().getName());
        }

    }
}
