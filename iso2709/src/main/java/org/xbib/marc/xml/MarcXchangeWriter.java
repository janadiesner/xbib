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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.util.XMLEventConsumer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MarcXchangeWriter writes MarcXchange events to a StaX XML output stream or writer
 */
public class MarcXchangeWriter extends MarcXchangeContentHandler
        implements MarcXchangeConstants, MarcXchangeListener {

    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private final static String NAMESPACE = MARCXCHANGE_V2_NS_URI;

    private final static String NAMESPACE_SCHEMA_LOCATION = MARCXCHANGE_V2_0_SCHEMALOCATION;

    private final static QName COLLECTION_ELEMENT = new QName(NAMESPACE, COLLECTION, "");

    private final static QName RECORD_ELEMENT = new QName(NAMESPACE, RECORD, "");

    private final static QName LEADER_ELEMENT = new QName(NAMESPACE, LEADER, "");

    private final static QName CONTROLFIELD_ELEMENT = new QName(NAMESPACE, CONTROLFIELD, "");

    private final static QName DATAFIELD_ELEMENT = new QName(NAMESPACE, DATAFIELD, "");

    private final static QName SUBFIELD_ELEMENT = new QName(NAMESPACE, SUBFIELD, "");

    private final static Namespace namespace = eventFactory.createNamespace("", NAMESPACE);

    private final static Iterator<Namespace> namespaces = Collections.singletonList(namespace).iterator();

    private final static Pattern NUMERIC_TAG = Pattern.compile("\\d\\d\\d");

    private final ReentrantLock lock = new ReentrantLock(true);

    private XMLEventConsumer writer;

    private XMLStreamException exception;

    private boolean documentStarted;

    private boolean schemaWritten;

    public MarcXchangeWriter setMarcXchangeListener(MarcXchangeListener listener) {
        super.setMarcXchangeListener(listener);
        return this;
    }

    public MarcXchangeWriter(OutputStream out) throws IOException {
        try {
            this.writer = outputFactory.createXMLEventWriter(out);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    public MarcXchangeWriter(Writer writer) throws IOException {
        try {
            this.writer = outputFactory.createXMLEventWriter(writer);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    public MarcXchangeWriter(XMLEventConsumer consumer) throws IOException {
        this.writer = consumer;
    }

    @Override
    public void startDocument() {
        if (exception != null) {
            return;
        }
        if (documentStarted) {
            return;
        }
        try {
            writer.add(eventFactory.createStartDocument("UTF-8", "1.0"));
            documentStarted = true;
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void endDocument() {
        if (exception != null) {
            return;
        }
        if (!documentStarted) {
            return;
        }
        try {
            writer.add(eventFactory.createEndDocument());
            documentStarted = false;
        } catch (XMLStreamException e) {
            exception = e;
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void beginCollection() {
        super.beginCollection();
        if (exception != null) {
            return;
        }
        try {
            Iterator<Attribute> attrs = schemaWritten ? null : Arrays.asList(
                eventFactory.createAttribute("xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI),
                eventFactory.createAttribute("xsi:schemaLocation", NAMESPACE + " " + NAMESPACE_SCHEMA_LOCATION)
            ).iterator();
            writer.add(eventFactory.createStartElement(COLLECTION_ELEMENT, attrs, namespaces));
            schemaWritten = true;
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void endCollection() {
        super.endCollection();
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
        super.beginRecord(format, type);
        if (exception != null) {
            return;
        }
        lock.lock();
        try {
            List<Attribute> attrs = Arrays.asList(
                    eventFactory.createAttribute(TYPE, type),
                    eventFactory.createAttribute(FORMAT, format)
            );
            if (!schemaWritten) {
                attrs.add(eventFactory.createAttribute("xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI));
                attrs.add(eventFactory.createAttribute("xsi:schemaLocation", NAMESPACE + " " + NAMESPACE_SCHEMA_LOCATION));
                schemaWritten = true;
            }
            writer.add(eventFactory.createStartElement(RECORD_ELEMENT, attrs.iterator(), namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void endRecord() {
        super.endRecord();
        if (exception != null) {
            return;
        }
        try {
            writer.add(eventFactory.createEndElement(RECORD_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void leader(String label) {
        super.leader(label);
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
        // check if this field is really a control field, if not, switch to data field
        if (!field.isControlField()) {
            field.indicator("  "); // two blank indicators
            beginDataField(field);
            inData = true;
            return;
        }
        super.beginControlField(field);
        if (exception != null) {
            return;
        }
        try {
            Iterator<Attribute> attrs = Collections.singletonList(eventFactory.createAttribute(TAG, field.tag())).iterator();
            writer.add(eventFactory.createStartElement(CONTROLFIELD_ELEMENT, attrs, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void endControlField(Field field) {
        // check if this field is really a control field, if not, switch to data field
        if (field != null && !field.isControlField()) {
            if (field.data() != null) {
                // if non-digit tag, add an "a" as subfield code
                Matcher m = NUMERIC_TAG.matcher(field.tag());
                if (m.matches()) {
                    // if numeric tag, create subfield code from first character in the data, it is already there.
                    field.subfieldId(field.data().substring(0, 1));
                    field.data(field.data().substring(1));
                } else {
                    field.subfieldId("a");
                }
                beginSubField(field);
                endSubField(field);
                field.data("");
            }
            endDataField(field);
            inData = false;
            return;
        }
        super.endControlField(field);
        if (exception != null) {
            return;
        }
        try {
            if (field != null && field.data() != null && !field.data().isEmpty()) {
                writer.add(eventFactory.createCharacters(field.data()));
            }
            writer.add(eventFactory.createEndElement(CONTROLFIELD_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    @Override
    public void beginDataField(Field field) {
        // check if this field is really a data field, if not, switch to control field
        if (field.isControlField()) {
            beginControlField(field);
            inControl = true;
            return;
        }
        // check if indicators are "-". Replace with blank " ".
        if (field.indicator().contains("-")) {
            field.indicator(field.indicator().replace('-',' '));
        }
        super.beginDataField(field);
        if (exception != null) {
            return;
        }
        try {
            // validate attribute values, must not be null, must not be empty
            String tag = field.tag() != null ? field.tag() : "999";
            String ind1 = field.indicator() != null && field.indicator().length() > 0 ? field.indicator().substring(0,1) : " ";
            String ind2 = field.indicator() != null && field.indicator().length() > 1 ? field.indicator().substring(1,2) : " ";
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
        // check if this field is really a data field, if not, switch to control field
        if (field != null && field.isControlField()) {
            endControlField(field);
            inControl = false;
            return;
        }
        super.endDataField(field);
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
        if (inControl || inLeader) {
            return;
        }
        super.beginSubField(field);
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
        try {
            if (inControl || inLeader) {
                if (field.data() != null) {
                    writer.add(eventFactory.createCharacters(field.data()));
                }
                return;
            }
            super.endSubField(field);
            if (exception != null) {
                return;
            }
            if (field != null && field.data() != null) {
                writer.add(eventFactory.createCharacters(field.data()));
            }
            writer.add(eventFactory.createEndElement(SUBFIELD_ELEMENT, namespaces));
        } catch (XMLStreamException e) {
            exception = e;
        }
    }

    public Exception getException() {
        return exception;
    }

}
