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

import org.xbib.marc.MarcXchangeListener;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

/**
 * This mapping MarcXchangeReader reads MarcXML or MarcXchange and fires events to a SAX content handler
 * or a MarcXchange listener
 */
public class MarcXchangeMappingReader extends MarcXchangeMappingContentHandler {

    private final SAXParser parser;

    private ContentHandler contentHandler;

    public MarcXchangeMappingReader() throws ParserConfigurationException, SAXException {
        this(SAXParserFactory.newInstance());
    }

    public MarcXchangeMappingReader(SAXParserFactory factory) throws ParserConfigurationException, SAXException {
        factory.setNamespaceAware(true);
        this.parser = factory.newSAXParser();
    }

    public void parse(InputSource source) throws SAXException, IOException {
        parser.getXMLReader().setContentHandler(contentHandler != null ? contentHandler : this);
        parser.getXMLReader().parse(source);
    }

    public MarcXchangeMappingReader setHandler(ContentHandler handler) {
        this.contentHandler = handler;
        return this;
    }

    public MarcXchangeMappingReader setMarcXchangeListener(MarcXchangeListener listener) {
        super.setMarcXchangeListener(listener);
        return this;
    }

    public XMLReader getXMLReader() throws SAXException {
        return parser.getXMLReader();
    }
}
