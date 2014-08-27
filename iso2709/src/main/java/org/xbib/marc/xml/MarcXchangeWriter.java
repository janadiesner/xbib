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
package org.xbib.marc.xml;

import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * The MarcXchangeReader reads MarcXML or MarcXchange and fires events to a SAX content handler
 * or a MarcXchange listener
 */
public class MarcXchangeWriter implements Closeable, Flushable, MarcXchangeConstants, MarcXchangeListener {

    public XMLEventWriter writer;

    private final static QName COLLECTION_ELEMENT = new QName(NS_URI, COLLECTION, NS_PREFIX);

    private final static QName RECORD_ELEMENT = new QName(NS_URI, RECORD, NS_PREFIX);

    private final static QName LEADER_ELEMENT = new QName(NS_URI, LEADER, NS_PREFIX);

    private final static QName CONTROLFIELD_ELEMENT = new QName(NS_URI, CONTROLFIELD, NS_PREFIX);

    private final static QName DATAFIELD_ELEMENT = new QName(NS_URI, DATAFIELD, NS_PREFIX);

    private final static QName SUBFIELD_ELEMENT = new QName(NS_URI, SUBFIELD, NS_PREFIX);

    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private final static Namespace namespace = eventFactory.createNamespace(NS_PREFIX, NS_URI);

    private final static Iterator<Namespace> namespaces = Collections.singletonList(namespace).iterator();

    private XMLStreamException exception;

    public MarcXchangeWriter(OutputStream out) throws XMLStreamException {
        this.writer = outputFactory.createXMLEventWriter(out);
    }

    public MarcXchangeWriter(Writer writer) throws XMLStreamException {
        this.writer = outputFactory.createXMLEventWriter(writer);
    }

    public void startDocument() {
        if (exception != null) {
            return;
        }
        try {
            writer.add(eventFactory.createStartDocument("UTF-8", "1.0"));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    public void endDocument() {
        if (exception != null) {
            return;
        }
        try {
            writer.add(eventFactory.createEndDocument());
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void beginCollection() {
        if (exception != null) {
            return;
        }
        try {
            //Attribute schemaLocation = eventFactory.createAttribute("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
            //        "schemaLocation", f + " http://www");
            writer.add(eventFactory.createStartElement(COLLECTION_ELEMENT, null, namespaces));

        } catch (XMLStreamException e) {
            exception = e;
        }    }

    @Override
    public void endCollection() {
        if (exception != null) {
            return;
        }
        try {
            writer.add(eventFactory.createEndElement(COLLECTION_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        if (exception != null) {
            return;
        }
        try {
            Iterator<Attribute> attrs = Arrays.asList(
                    eventFactory.createAttribute(TYPE, type),
                    eventFactory.createAttribute(FORMAT, format)
            ).iterator();
            writer.add(eventFactory.createStartElement(RECORD_ELEMENT, attrs, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void endRecord() {
        if (exception != null) {
            return;
        }
        try {
            writer.add(eventFactory.createEndElement(RECORD_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void leader(String label) {
        if (exception != null) {
            return;
        }
        try {
            writer.add(eventFactory.createStartElement(LEADER_ELEMENT, null, namespaces));
            writer.add(eventFactory.createCharacters(label));
            writer.add(eventFactory.createEndElement(LEADER_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void beginControlField(Field field) {
        if (exception != null) {
            return;
        }
        try {
            Iterator<Attribute> attrs = Collections.singletonList(eventFactory.createAttribute(TAG, field.tag())).iterator();
            writer.add(eventFactory.createStartElement(CONTROLFIELD_ELEMENT, attrs, namespaces));
            writer.add(eventFactory.createCharacters(field.data()));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void endControlField(Field field) {
        if (exception != null) {
            return;
        }
        try {
            writer.add(eventFactory.createEndElement(CONTROLFIELD_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void beginDataField(Field field) {
        if (exception != null) {
            return;
        }
        if (field == null) {
            return;
        }
        try {
            // validate attribute values, must not be null
            String tag = field.tag() != null ? field.tag() : "";
            String ind1 = field.indicator() != null && field.indicator().length() > 0 ? field.indicator().substring(0,1) : "";
            String ind2 = field.indicator() != null && field.indicator().length() > 1 ? field.indicator().substring(1,2) : "";
            Iterator<Attribute> attrs = Arrays.asList(
                    eventFactory.createAttribute(TAG, tag),
                    eventFactory.createAttribute(IND + "1", ind1),
                    eventFactory.createAttribute(IND + "2", ind2)
            ).iterator();
            writer.add(eventFactory.createStartElement(DATAFIELD_ELEMENT, attrs, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void endDataField(Field field) {
        if (exception != null) {
            return;
        }
        try {
            if (field != null && field.data() != null && !field.data().isEmpty()) {
                writer.add(eventFactory.createCharacters(field.data()));
            }
            writer.add(eventFactory.createEndElement(DATAFIELD_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void beginSubField(Field field) {
        if (exception != null) {
            return;
        }
        try {
            Iterator<Attribute> attrs = Collections.singletonList(
                    eventFactory.createAttribute(CODE, field.subfieldId())
            ).iterator();
            writer.add(eventFactory.createStartElement(SUBFIELD_ELEMENT, attrs, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }

    }

    @Override
    public void endSubField(Field field) {
        if (exception != null) {
            return;
        }
        try {
            if (field != null) {
                writer.add(eventFactory.createCharacters(field.data()));
            }
            writer.add(eventFactory.createEndElement(SUBFIELD_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            writer.flush();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    public Exception getException() {
        return exception;
    }


}
