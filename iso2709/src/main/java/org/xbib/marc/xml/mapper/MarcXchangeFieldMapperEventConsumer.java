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
package org.xbib.marc.xml.mapper;

import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * The MarcXchange mapping event consumer receives StaX events, maps the MarcXchange fields to
 * other fields, and fiels MarcXchange events
 */
public class MarcXchangeFieldMapperEventConsumer
    extends MarcXchangeFieldMapper
    implements XMLEventConsumer, MarcXchangeConstants {

    private Stack<Field> stack = new Stack<Field>();

    private Map<String,MarcXchangeListener> listeners = new HashMap<String,MarcXchangeListener>();

    private MarcXchangeListener listener;

    private StringBuilder content = new StringBuilder();

    private String format = "MARC21";

    private String type = "Bibliographic";

    protected boolean inData;

    protected boolean inLeader;

    protected boolean inControl;

    private boolean ignoreNamespace = false;

    private Set<String> validNamespaces = new HashSet<String>() {{
        add(MARCXCHANGE_V1_NS_URI);
        add(MARCXCHANGE_V2_NS_URI);
        add(MARC21_NS_URI);
    }};

    public MarcXchangeFieldMapperEventConsumer addListener(String type, MarcXchangeListener listener) {
        this.listeners.put(type, listener);
        return this;
    }

    public MarcXchangeFieldMapperEventConsumer setMarcXchangeListener(MarcXchangeListener listener) {
        this.listeners.put("Bibliographic", listener);
        return this;
    }

    public MarcXchangeFieldMapperEventConsumer setFormat(String format) {
        this.format = format;
        return this;
    }

    public MarcXchangeFieldMapperEventConsumer setType(String type) {
        this.type = type;
        return this;
    }

    public MarcXchangeFieldMapperEventConsumer setIgnoreNamespace(boolean ignore) {
        this.ignoreNamespace = ignore;
        return this;
    }

    public MarcXchangeFieldMapperEventConsumer addNamespace(String uri) {
        this.validNamespaces.add(uri);
        return this;
    }

    @Override
    public void beginCollection() {
        if (listener != null) {
            listener.beginCollection();
        }
    }

    @Override
    public void endCollection() {
        if (listener != null) {
            listener.endCollection();
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        this.listener = listeners.get(type);
        if (listener != null) {
            listener.beginRecord(format, type);
        }
    }

    @Override
    public void endRecord() {
        if (listener != null) {
            listener.endRecord();
        }
    }

    @Override
    public void leader(String label) {
        if (listener != null) {
            listener.leader(label);
        }
    }

    @Override
    public void beginControlField(Field field) {
        if (listener != null) {
            listener.beginControlField(field);
        }
    }

    @Override
    public void endControlField(Field field) {
        if (listener != null) {
            listener.endControlField(field);
        }
    }

    @Override
    public void beginDataField(Field field) {
        if (listener != null) {
            listener.beginDataField(field);
        }
    }

    @Override
    public void endDataField(Field field) {
        if (listener != null) {
            listener.endDataField(field);
        }
    }

    @Override
    public void beginSubField(Field field) {
        if (listener != null) {
            listener.beginSubField(field);
        }
    }

    @Override
    public void endSubField(Field field) {
        if (listener != null) {
            listener.endSubField(field);
        }
    }

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            String uri = element.getName().getNamespaceURI();
            if (!isNamespace(uri)) {
                return;
            }
            String localName = element.getName().getLocalPart();
            Iterator<?> it = element.getAttributes();
            String format = null;
            String type = null;
            String tag = null;
            char ind1 = '\u0000';
            char ind2 = '\u0000';
            char code = '\u0000';
            while (it.hasNext()) {
                Attribute attr = (Attribute) it.next();
                QName attributeName = attr.getName();
                String attributeLocalName = attributeName.getLocalPart();
                String attributeValue = attr.getValue();
                switch (attributeLocalName) {
                    case TAG: {
                        tag = attributeValue;
                        break;
                    }
                    case IND + "1": {
                        ind1 = attributeValue.charAt(0);
                        if (ind1 == '-') {
                            ind1 = ' '; // replace illegal '-' symbols
                        }
                        break;
                    }
                    case IND + "2": {
                        ind2 = attributeValue.charAt(0);
                        if (ind2 == '-') {
                            ind2 = ' '; // replace illegal '-' symbols
                        }
                        break;
                    }
                    case CODE: {
                        code = attributeValue.charAt(0);
                        break;
                    }
                    case FORMAT: {
                        format = attributeValue;
                        break;
                    }
                    case TYPE: {
                        type = attributeValue;
                        break;
                    }
                }
            }
            if (format == null) {
                format = this.format;
            }
            if (type == null) {
                type = this.type;
            }
            switch (localName) {
                case COLLECTION: {
                    beginCollection();
                    break;
                }
                case RECORD:{
                    setFormat(format);
                    setType(type);
                    break;
                }
                case LEADER: {
                    inLeader = true;
                    break;
                }
                case CONTROLFIELD: {
                    Field field = new Field(tag);
                    stack.push(field);
                    addControlField(field);
                    content.setLength(0);
                    inControl = true;
                    break;
                }
                case DATAFIELD: {
                    Field field = ind2 != '\u0000'
                            ? new Field(tag, Character.toString(ind1) + Character.toString(ind2))
                            : new Field(tag, Character.toString(ind1));
                    stack.push(field);
                    addDataField(field);
                    content.setLength(0);
                    inData = true;
                    break;
                }
                case SUBFIELD: {
                    if (inControl || inLeader) {
                        break;
                    } else {
                        Field f = stack.peek();
                        Field subfield = new Field(f.tag(), f.indicator(), Character.toString(code));
                        stack.push(subfield);
                        addDataField(subfield);
                        content.setLength(0);
                        break;
                    }
                }

            }
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            String uri = element.getName().getNamespaceURI();
            if (!isNamespace(uri)) {
                content.setLength(0);
                return;
            }
            String localName = element.getName().getLocalPart();
            switch (localName) {
                case COLLECTION: {
                    endCollection();
                    break;
                }
                case RECORD: {
                    flushRecord();
                    break;
                }
                case LEADER: {
                    setRecordLabel(content.toString());
                    inLeader = false;
                    break;
                }
                case CONTROLFIELD: {
                    addControlField(stack.pop().data(content.toString()));
                    inControl = false;
                    break;
                }
                case DATAFIELD: {
                    addDataField(stack.pop());
                    flushField();
                    inData = false;
                    break;
                }
                case SUBFIELD: {
                    if (inControl) {
                        // repair, move data to controlfield or leader
                        stack.peek().data(content.toString());
                        break;
                    } else {
                        addDataField(stack.pop().data(content.toString()));
                        inData = false;
                        break;
                    }
                }
            }
            content.setLength(0);
        } else if (event.isCharacters()) {
            Characters c = (Characters) event;
            if (!c.isIgnorableWhiteSpace()) {
                if (inData || inControl || inLeader) {
                    content.append(c.getData());
                }
            }
        } else if (event.isStartDocument()) {
            stack.clear();
            content.setLength(0);
        }
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    private boolean isNamespace(String uri) {
        return !ignoreNamespace && validNamespaces.contains(uri);
    }

}
