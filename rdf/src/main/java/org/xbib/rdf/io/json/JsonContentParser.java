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
package org.xbib.rdf.io.json;

import org.xbib.json.xml.JsonSaxAdapter;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParser;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Reader;

/**
 * A parser for generic JSON (not JSON-LD)
 */
public class JsonContentParser implements RdfContentParser {

    private XmlHandler handler;

    private RdfContentBuilder builder;

    private QName root;

    public JsonContentParser() {
    }

    public JsonContentParser setHandler(XmlHandler handler) {
        this.handler = handler;
        return this;
    }

    public XmlHandler getHandler() {
        return handler;
    }

    public JsonContentParser root(QName root) {
        this.root = root;
        return this;
    }

    public JsonContentParser builder(RdfContentBuilder builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public JsonContentParser parse(Reader reader) throws IOException {
        if (handler != null) {
            if (builder != null) {
                handler.setBuilder(builder);
            }
            JsonSaxAdapter adapter = new JsonSaxAdapter(reader, handler)
                    .root(root)
                    .context(handler.getNamespaceContext());
            try {
                adapter.parse();
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }
        return this;
    }

}
