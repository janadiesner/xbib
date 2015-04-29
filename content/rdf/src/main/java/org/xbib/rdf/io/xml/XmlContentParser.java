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

import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParser;
import org.xbib.rdf.RdfContentType;
import org.xbib.rdf.StandardRdfContentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * An XML reader for parsing XML into triples
 */
public class XmlContentParser implements RdfContentParser {

    private final Reader reader;

    private RdfContentBuilder builder;

    private XmlHandler handler;

    private boolean namespaces = true;

    private boolean validate = false;

    public XmlContentParser(InputStream in) throws IOException {
        this(new InputStreamReader(in, "UTF-8"));
    }

    public XmlContentParser(Reader reader) {
        this.reader = reader;
    }

    @Override
    public RdfContentType contentType() {
        return StandardRdfContentType.XML;
    }

    public XmlContentParser setHandler(XmlHandler handler) {
        this.handler = handler;
        return this;
    }

    public XmlHandler getHandler() {
        return handler;
    }

    public XmlContentParser setValidate(boolean validate) {
        this.validate = validate;
        return this;
    }

    public XmlContentParser setNamespaces(boolean namespaces) {
        this.namespaces = namespaces;
        return this;
    }

    public XmlContentParser builder(RdfContentBuilder builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public XmlContentParser parse() throws IOException {
        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            parse(xmlReader, new InputSource(reader));
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        return this;
    }

    private XmlContentParser parse(XMLReader reader, InputSource source) throws IOException, SAXException {
        if (handler != null) {
            if (builder != null) {
                handler.setBuilder(builder);
            }
            reader.setContentHandler(handler);
        }
        reader.setFeature("http://xml.org/sax/features/namespaces", namespaces);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", validate);
        reader.parse(source);
        return this;
    }
}
